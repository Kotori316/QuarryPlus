package com.yogpc.qp.machines.marker;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import com.yogpc.qp.Holder;
import com.yogpc.qp.machines.Area;
import com.yogpc.qp.machines.CheckerLog;
import com.yogpc.qp.machines.QuarryMarker;
import com.yogpc.qp.packet.ClientSync;
import com.yogpc.qp.packet.ClientSyncMessage;
import com.yogpc.qp.packet.PacketHandler;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TileMarker extends BlockEntity implements QuarryMarker, CheckerLog, ClientSync {
    public static final int MAX_SEARCH = 256;
    private MarkerConnection markerConnection = MarkerConnection.EMPTY;
    public boolean rsReceiving;

    public TileMarker(BlockPos pos, BlockState state) {
        super(Holder.MARKER_TYPE, pos, state);
    }

    void tryConnect(boolean first) {
        assert getLevel() != null;
        Optional<TileMarker> zMarker = IntStream.range(1, MAX_SEARCH)
            .flatMap(i -> IntStream.of(i, -i))
            .mapToObj(i -> getBlockPos().relative(Direction.NORTH, i))
            .flatMap(p -> getLevel().getBlockEntity(p, Holder.MARKER_TYPE).stream())
            .findFirst();
        Optional<TileMarker> xMarker = IntStream.range(1, MAX_SEARCH)
            .flatMap(i -> IntStream.of(i, -i))
            .mapToObj(i -> getBlockPos().relative(Direction.EAST, i))
            .flatMap(p -> getLevel().getBlockEntity(p, Holder.MARKER_TYPE).stream())
            .findFirst();
        MarkerConnection.set(this, xMarker.orElse(null), zMarker.orElse(null));
        if (first && this.markerConnection == MarkerConnection.EMPTY) {
            xMarker.ifPresent(marker -> marker.tryConnect(false));
        }
        if (first && this.markerConnection == MarkerConnection.EMPTY) {
            zMarker.ifPresent(marker -> marker.tryConnect(false));
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null && !level.isClientSide)
            markerConnection.markerPlaces().stream().flatMap(p -> level.getBlockEntity(p, Holder.MARKER_TYPE).stream())
                .forEach(TileMarker::resetConnection);
    }

    private static void resetConnection(TileMarker m) {
        m.markerConnection = MarkerConnection.EMPTY;
        m.sync();
    }

    @Override
    public Optional<Area> getArea() {
        return markerConnection.getArea();
    }

    @OnlyIn(Dist.CLIENT)
    public Optional<Area> renderArea() {
        if (markerConnection.render()) return getArea();
        else return Optional.empty();
    }

    @Override
    public List<ItemStack> removeAndGetItems() {
        assert getLevel() != null;
        var count = markerConnection.markerPlaces().size();
        markerConnection.markerPlaces().forEach(p -> getLevel().removeBlock(p, false));
        return List.of(new ItemStack(Holder.BLOCK_MARKER, count));
    }

    @Override
    public List<? extends Component> getDebugLogs() {
        return List.of(
            new TextComponent("%sMarker Area%s: %s".formatted(ChatFormatting.AQUA, ChatFormatting.RESET, markerConnection.getArea())),
            new TextComponent("%sMarker Poses%s: %s".formatted(ChatFormatting.AQUA, ChatFormatting.RESET, markerConnection.markerPlaces()))
        );
    }

    @Override
    public void fromClientTag(CompoundTag tag) {
        this.markerConnection = MarkerConnection.fromClientNbt(tag.getCompound("markerConnection"));
        this.rsReceiving = tag.getBoolean("rsReceiving");
    }

    @Override
    public CompoundTag toClientTag(CompoundTag tag) {
        tag.put("markerConnection", markerConnection.toClientNbt());
        tag.putBoolean("rsReceiving", rsReceiving);
        return tag;
    }

    void sync() {
        if (level != null && !level.isClientSide)
            PacketHandler.sendToClient(new ClientSyncMessage(this), level);
    }

    record MarkerConnection(@Nullable Area area, @Nonnull Set<BlockPos> markerPlaces, boolean render) {
        static final MarkerConnection EMPTY = new MarkerConnection(null, Collections.emptySet(), false);

        static void set(TileMarker thisMarker, @Nullable TileMarker xMarker, @Nullable TileMarker zMarker) {
            if (xMarker != null && zMarker != null) {
                var connectionParent = new MarkerConnection(new Area(xMarker.getBlockPos(), zMarker.getBlockPos().above(4), Direction.UP),
                    Set.of(thisMarker.getBlockPos(), xMarker.getBlockPos(), zMarker.getBlockPos()), true);
                var connectionChild = new MarkerConnection(new Area(xMarker.getBlockPos(), zMarker.getBlockPos().above(4), Direction.UP),
                    Set.of(thisMarker.getBlockPos(), xMarker.getBlockPos(), zMarker.getBlockPos()), false);

                thisMarker.markerConnection = connectionParent;
                xMarker.markerConnection = connectionChild;
                zMarker.markerConnection = connectionChild;
                // Send changes to Client
                thisMarker.sync();
                xMarker.sync();
                zMarker.sync();
            }
        }

        Optional<Area> getArea() {
            return Optional.ofNullable(area);
        }

        CompoundTag toClientNbt() {
            var tag = new CompoundTag();
            if (area != null) tag.put("area", area.toNBT());
            tag.putBoolean("render", render);
            return tag;
        }

        static MarkerConnection fromClientNbt(CompoundTag tag) {
            var area = tag.contains("area") ? Area.fromNBT(tag.getCompound("area")) : Optional.<Area>empty();
            if (area.isEmpty()) return EMPTY;
            else return new MarkerConnection(area.orElse(null), Collections.emptySet(), tag.getBoolean("render"));
        }
    }

}
