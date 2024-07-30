package com.yogpc.qp.machine.marker;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.machine.Area;
import com.yogpc.qp.machine.QpBlock;
import com.yogpc.qp.packet.ClientSync;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class NormalMarkerEntity extends BlockEntity implements QuarryMarker, ClientSync {
    public static final int MAX_SEARCH = 256;
    @NotNull
    private Status status = Status.NOT_CONNECTED;
    @Nullable
    private Link link;

    public NormalMarkerEntity(BlockPos pos, BlockState blockState) {
        super(PlatformAccess.getAccess().registerObjects().getBlockEntityType((QpBlock) blockState.getBlock()).orElseThrow(),
            pos, blockState);
    }

    void tryConnect(Consumer<Component> messageSender) {
        if (this.status.isConnected()) {
            messageSender.accept(Component.literal("This marker already has connection"));
            return;
        }
        assert level != null;
        var xMarker = getMarker(level, getBlockPos(), this.getType(), Direction.Axis.X);
        var zMarker = getMarker(level, getBlockPos(), this.getType(), Direction.Axis.Z);
        var yMarker = IntStream.range(1, MAX_SEARCH)
            .flatMap(i -> IntStream.of(i, -i))
            .filter(y -> !level.isOutsideBuildHeight(y))
            .boxed()
            .flatMap(d ->
                Stream.concat(Stream.of(this.getBlockPos()), Stream.concat(xMarker.stream(), zMarker.stream()).map(BlockEntity::getBlockPos))
                    .map(p -> p.relative(Direction.Axis.Y, d))
                    .flatMap(p -> level.getBlockEntity(p, this.getType()).stream())
            )
            .filter(NormalMarkerEntity.class::isInstance)
            .map(NormalMarkerEntity.class::cast)
            .filter(e -> !e.status.isConnected())
            .findAny();

        var maybeLink = xMarker.flatMap(x -> zMarker.map(z -> {
            var y = yMarker.map(m -> m.getBlockPos().getY()).orElse(getBlockPos().getY() + 4);
            return new Link(getBlockPos(), x.getBlockPos(), z.getBlockPos().atY(y));
        }));
        maybeLink.ifPresentOrElse(link -> {
            setLink(link, true);
            Stream.of(xMarker, yMarker, zMarker)
                .flatMap(Optional::stream)
                .forEach(e -> e.setLink(link, false));
            messageSender.accept(Component.literal("Marker successfully established connection"));
        }, () -> messageSender.accept(Component.literal("Marker tried to establish connection, but failed")));
    }

    static Optional<NormalMarkerEntity> getMarker(Level level, BlockPos pos, BlockEntityType<?> type, Direction.Axis axis) {
        return IntStream.range(1, MAX_SEARCH)
            .flatMap(i -> IntStream.of(i, -i))
            .mapToObj(d -> pos.relative(axis, d))
            .flatMap(p -> level.getBlockEntity(p, type).stream())
            .filter(NormalMarkerEntity.class::isInstance)
            .map(NormalMarkerEntity.class::cast)
            .filter(e -> !e.status.isConnected())
            .findFirst();
    }

    @Override
    public Optional<QuarryMarker.Link> getLink() {
        return Optional.ofNullable(link);
    }

    void setLink(@Nullable Link link, boolean isMaster) {
        this.link = link;
        if (link != null) {
            this.status = isMaster ? Status.CONNECTED_MASTER : Status.CONNECTED_SLAVE;
        } else {
            this.status = Status.NOT_CONNECTED;
        }
        syncToClient();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putString("status", status.name());
        super.saveAdditional(tag, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        var status = Status.valueOf(tag.getString("status"));
        if (status == Status.RS_POWERED) {
            this.status = status;
        } else {
            this.status = Status.NOT_CONNECTED;
        }
    }

    @Override
    public void fromClientTag(CompoundTag tag) {
        status = Status.valueOf(tag.getString("status"));
        link = Link.CODEC.codec().parse(NbtOps.INSTANCE, tag.get("link")).result().orElse(null);
    }

    @Override
    public CompoundTag toClientTag(CompoundTag tag) {
        tag.putString("status", status.name());
        if (link != null) {
            tag.put("link", Link.CODEC.codec().encodeStart(NbtOps.INSTANCE, link).getOrThrow());
        }
        return tag;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (this.link != null && this.level != null && !this.level.isClientSide()) {
            Stream.of(link.master, link.s1, link.s2)
                .map(level::getBlockEntity)
                .filter(NormalMarkerEntity.class::isInstance)
                .map(NormalMarkerEntity.class::cast)
                .forEach(m -> m.setLink(null, false));
        }
    }

    public enum Status {
        NOT_CONNECTED,
        RS_POWERED,
        CONNECTED_MASTER,
        CONNECTED_SLAVE,
        ;

        boolean isConnected() {
            return this == CONNECTED_MASTER || this == CONNECTED_SLAVE;
        }
    }

    protected record Link(BlockPos master, BlockPos s1, BlockPos s2) implements QuarryMarker.Link {
        static final MapCodec<Link> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            RecordCodecBuilder.of(Link::master, "master", BlockPos.CODEC),
            RecordCodecBuilder.of(Link::s1, "s1", BlockPos.CODEC),
            RecordCodecBuilder.of(Link::s2, "s2", BlockPos.CODEC)
        ).apply(i, Link::new));

        @Override
        public Area area() {
            var minX = Math.min(master.getX(), Math.min(s1.getX(), s2.getX()));
            var minY = Math.min(master.getY(), Math.min(s1.getY(), s2.getY()));
            var minZ = Math.min(master.getZ(), Math.min(s1.getZ(), s2.getZ()));
            var maxX = Math.max(master.getX(), Math.max(s1.getX(), s2.getX()));
            var maxY = Math.max(master.getY(), Math.max(s1.getY(), s2.getY()));
            var maxZ = Math.max(master.getZ(), Math.max(s1.getZ(), s2.getZ()));
            return new Area(minX, minY, minZ, maxX, maxY, maxZ, Direction.UP);
        }

        @Override
        public void remove(Level level) {
            for (BlockPos pos : List.of(master, s1, s2)) {
                level.removeBlock(pos, false);
            }
        }

        @Override
        public List<ItemStack> drops() {
            var stack = new ItemStack(PlatformAccess.getAccess().registerObjects().markerBlock().get(), 3);
            return List.of(stack);
        }
    }
}
