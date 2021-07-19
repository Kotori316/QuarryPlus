package com.yogpc.qp.machines.marker;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.Area;
import com.yogpc.qp.machines.CheckerLog;
import com.yogpc.qp.machines.QuarryMarker;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.block.entity.BlockEntityClientSerializable;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TileMarker extends BlockEntity implements QuarryMarker, CheckerLog, BlockEntityClientSerializable {
    public static final int MAX_SEARCH = 256;
    private MarkerConnection markerConnection = MarkerConnection.EMPTY;
    public boolean rsReceiving;

    public TileMarker(BlockPos pos, BlockState state) {
        super(QuarryPlus.ModObjects.MARKER_TYPE, pos, state);
    }

    void tryConnect() {
        assert getWorld() != null;
        Optional<TileMarker> zMarker = IntStream.range(1, MAX_SEARCH)
            .flatMap(i -> IntStream.of(i, -i))
            .mapToObj(i -> getPos().offset(Direction.NORTH, i))
            .flatMap(p -> getWorld().getBlockEntity(p, QuarryPlus.ModObjects.MARKER_TYPE).stream())
            .findFirst();
        Optional<TileMarker> xMarker = IntStream.range(1, MAX_SEARCH)
            .flatMap(i -> IntStream.of(i, -i))
            .mapToObj(i -> getPos().offset(Direction.EAST, i))
            .flatMap(p -> getWorld().getBlockEntity(p, QuarryPlus.ModObjects.MARKER_TYPE).stream())
            .findFirst();
        MarkerConnection.set(this, xMarker.orElse(null), zMarker.orElse(null));
        if (this.markerConnection == MarkerConnection.EMPTY) {
            xMarker.ifPresent(TileMarker::tryConnect);
        }
        if (this.markerConnection == MarkerConnection.EMPTY) {
            zMarker.ifPresent(TileMarker::tryConnect);
        }
    }

    @Override
    public void markRemoved() {
        super.markRemoved();
        if (world != null && !world.isClient)
            markerConnection.markerPlaces().stream().flatMap(p -> world.getBlockEntity(p, QuarryPlus.ModObjects.MARKER_TYPE).stream())
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
    public Optional<Area> renderArea() {
        if (markerConnection.render()) return getArea();
        else return Optional.empty();
    }

    @Override
    public List<ItemStack> removeAndGetItems() {
        assert getWorld() != null;
        var count = markerConnection.markerPlaces().size();
        markerConnection.markerPlaces().forEach(p -> getWorld().removeBlock(p, false));
        return List.of(new ItemStack(QuarryPlus.ModObjects.BLOCK_MARKER, count));
    }

    @Override
    public List<? extends Text> getDebugLogs() {
        return List.of(
            new LiteralText("%sMarker Area%s: %s".formatted(Formatting.AQUA, Formatting.RESET, markerConnection.getArea())),
            new LiteralText("%sMarker Poses%s: %s".formatted(Formatting.AQUA, Formatting.RESET, markerConnection.markerPlaces()))
        );
    }

    @Override
    public void fromClientTag(NbtCompound tag) {
        this.markerConnection = MarkerConnection.fromClientNbt(tag.getCompound("markerConnection"));
        this.rsReceiving = tag.getBoolean("rsReceiving");
    }

    @Override
    public NbtCompound toClientTag(NbtCompound tag) {
        tag.put("markerConnection", markerConnection.toClientNbt());
        tag.putBoolean("rsReceiving", rsReceiving);
        return tag;
    }

    record MarkerConnection(@Nullable Area area, @NotNull Set<BlockPos> markerPlaces, boolean render) {
        static final MarkerConnection EMPTY = new MarkerConnection(null, Collections.emptySet(), false);

        static void set(TileMarker thisMarker, @Nullable TileMarker xMarker, @Nullable TileMarker zMarker) {
            if (xMarker != null && zMarker != null) {
                var connectionParent = new MarkerConnection(new Area(xMarker.getPos(), zMarker.getPos().up(4), Direction.UP),
                    Set.of(thisMarker.getPos(), xMarker.getPos(), zMarker.getPos()), true);
                var connectionChild = new MarkerConnection(new Area(xMarker.getPos(), zMarker.getPos().up(4), Direction.UP),
                    Set.of(thisMarker.getPos(), xMarker.getPos(), zMarker.getPos()), false);

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

        NbtCompound toClientNbt() {
            var tag = new NbtCompound();
            if (area != null) tag.put("area", area.toNBT());
            tag.putBoolean("render", render);
            return tag;
        }

        static MarkerConnection fromClientNbt(NbtCompound tag) {
            var area = tag.contains("area") ? Area.fromNBT(tag.getCompound("area")) : Optional.<Area>empty();
            if (area.isEmpty()) return EMPTY;
            else return new MarkerConnection(area.orElse(null), Collections.emptySet(), tag.getBoolean("render"));
        }
    }

}
