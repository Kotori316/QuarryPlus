package com.yogpc.qp.machines.marker;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.Area;
import com.yogpc.qp.machines.CheckerLog;
import com.yogpc.qp.machines.QuarryMarker;
import com.yogpc.qp.packet.ClientSync;
import com.yogpc.qp.render.Box;
import com.yogpc.qp.render.RenderMarker;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileMarker extends BlockEntity implements QuarryMarker, CheckerLog, ClientSync {
    public static final int MAX_SEARCH = 256;
    private MarkerConnection markerConnection = MarkerConnection.EMPTY;
    @Nullable
    private RenderBox renderBox;
    public boolean rsReceiving;

    public TileMarker(BlockPos pos, BlockState state) {
        super(QuarryPlus.ModObjects.MARKER_TYPE, pos, state);
    }

    void tryConnect(boolean first) {
        assert getLevel() != null;
        Optional<TileMarker> zMarker = IntStream.range(1, MAX_SEARCH)
            .flatMap(i -> IntStream.of(i, -i))
            .mapToObj(i -> getBlockPos().relative(Direction.NORTH, i))
            .flatMap(p -> getLevel().getBlockEntity(p, QuarryPlus.ModObjects.MARKER_TYPE).stream())
            .findFirst();
        Optional<TileMarker> xMarker = IntStream.range(1, MAX_SEARCH)
            .flatMap(i -> IntStream.of(i, -i))
            .mapToObj(i -> getBlockPos().relative(Direction.EAST, i))
            .flatMap(p -> getLevel().getBlockEntity(p, QuarryPlus.ModObjects.MARKER_TYPE).stream())
            .findFirst();
        Optional<TileMarker> yMarker = IntStream.range(1, MAX_SEARCH)
            .flatMap(i -> IntStream.of(i, -i))
            .filter(y -> !getLevel().isOutsideBuildHeight(y))
            .boxed()
            .flatMap(y -> Stream.concat(Stream.of(this.getBlockPos()), Stream.concat(xMarker.stream(), zMarker.stream()).map(TileMarker::getBlockPos))
                .map(p -> p.relative(Direction.Axis.Y, y))
                .flatMap(p -> getLevel().getBlockEntity(p, QuarryPlus.ModObjects.MARKER_TYPE).stream()))
            .findFirst();
        MarkerConnection.set(this, xMarker.orElse(null), zMarker.orElse(null), yMarker.orElse(null));
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
            markerConnection.markerPlaces().stream().flatMap(p -> level.getBlockEntity(p, QuarryPlus.ModObjects.MARKER_TYPE).stream())
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

    @Environment(EnvType.CLIENT)
    public Optional<Box[]> renderArea() {
        if (markerConnection.render()) {
            if (this.renderBox == null || this.renderBox.parent != this.markerConnection) {
                if (markerConnection.area != null) {
                    this.renderBox = new RenderBox(markerConnection, RenderMarker.getRenderBox(markerConnection.area));
                    return Optional.of(renderBox.boxes);
                } else {
                    return Optional.empty();
                }
            } else {
                return Optional.of(renderBox.boxes);
            }
        } else {
            return Optional.empty();
        }
    }

    @Override
    public List<ItemStack> removeAndGetItems() {
        assert getLevel() != null;
        var drops = markerConnection.markerPlaces().stream().map(getLevel()::getBlockState).map(BlockState::getBlock).map(ItemStack::new).toList();
        markerConnection.markerPlaces().forEach(p -> getLevel().removeBlock(p, false));
        return drops;
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

    record MarkerConnection(@Nullable Area area, @NotNull Set<BlockPos> markerPlaces, boolean render) {
        static final MarkerConnection EMPTY = new MarkerConnection(null, Collections.emptySet(), false);

        static void set(TileMarker thisMarker, @Nullable TileMarker xMarker, @Nullable TileMarker zMarker, @Nullable TileMarker yMarker) {
            if (xMarker != null && zMarker != null) {
                var area = new Area(
                    xMarker.getBlockPos(),
                    yMarker != null ? zMarker.getBlockPos().atY(yMarker.getBlockPos().getY()) : zMarker.getBlockPos().above(4),
                    Direction.UP);
                var connectionParent = new MarkerConnection(area,
                    Set.of(thisMarker.getBlockPos(), xMarker.getBlockPos(), zMarker.getBlockPos()), true);
                var connectionChild = new MarkerConnection(area,
                    Set.of(thisMarker.getBlockPos(), xMarker.getBlockPos(), zMarker.getBlockPos()), false);

                thisMarker.markerConnection = connectionParent;
                xMarker.markerConnection = connectionChild;
                zMarker.markerConnection = connectionChild;
                // Send changes to Client
                thisMarker.sync();
                xMarker.sync();
                zMarker.sync();
                if (yMarker != null) {
                    yMarker.markerConnection = connectionChild;
                    yMarker.sync();
                }
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

    record RenderBox(MarkerConnection parent, Box[] boxes) {
    }
}
