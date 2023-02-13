package com.yogpc.qp.machines;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record Area(int minX, int minY, int minZ, int maxX, int maxY, int maxZ,
                   Direction direction) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    public Area {
        if (minX > maxX)
            throw new IllegalArgumentException("MinX(%d) must be less than or equal to MaxX(%d)".formatted(minX, maxX));
        if (minY > maxY)
            throw new IllegalArgumentException("MinY(%d) must be less than or equal to MaxY(%d)".formatted(minY, maxY));
        if (minZ > maxZ)
            throw new IllegalArgumentException("MinZ(%d) must be less than or equal to MaxZ(%d)".formatted(minZ, maxZ));
    }

    public Area(Vec3i pos1, Vec3i pos2, Direction direction) {
        this(Math.min(pos1.getX(), pos2.getX()), Math.min(pos1.getY(), pos2.getY()), Math.min(pos1.getZ(), pos2.getZ()),
            Math.max(pos1.getX(), pos2.getX()), Math.max(pos1.getY(), pos2.getY()), Math.max(pos1.getZ(), pos2.getZ()), direction);
    }

    public Area assureY(int minSpaceY) {
        var space = maxY - minY;
        if (space >= minSpaceY)
            return this;
        else
            return new Area(minX, minY, minZ, maxX, minY + minSpaceY, maxZ, direction);
    }

    public Area aboveY(int minY) {
        if (this.minY < minY && minY <= this.maxY) {
            return new Area(minX, minY, minZ, maxX, maxY, maxZ, direction);
        } else {
            return this;
        }
    }

    public Area shrink(int x, int y, int z) {
        int x1 = minX + x;
        int x2 = maxX - x;
        int y1 = minY + y;
        int y2 = maxY - y;
        int z1 = minZ + z;
        int z2 = maxZ - z;
        return new Area(
            Math.min(x1, x2), Math.min(y1, y2), Math.min(z1, z2),
            Math.max(x1, x2), Math.max(y1, y2), Math.max(z1, z2),
            direction);
    }

    public boolean isInAreaIgnoreY(Vec3i vec3i) {
        return minX < vec3i.getX() && vec3i.getX() < maxX && minZ < vec3i.getZ() && vec3i.getZ() < maxZ;
    }

    public boolean isRangeInLimit(int limit, boolean isAdvQuarry) {
        if (limit <= 0) return true;
        int offset = isAdvQuarry ? 2 : 0;
        return (maxX - minX - offset) <= limit && (maxZ - minZ - offset) <= limit;
    }

    public int sizeOfEachY() {
        return (maxX() - minX()) * (maxZ() - minZ());
    }

    public CompoundTag toNBT() {
        var tag = new CompoundTag();
        tag.putInt("minX", this.minX);
        tag.putInt("minY", this.minY);
        tag.putInt("minZ", this.minZ);
        tag.putInt("maxX", this.maxX);
        tag.putInt("maxY", this.maxY);
        tag.putInt("maxZ", this.maxZ);
        tag.putString("direction", this.direction.getName());
        return tag;
    }

    public static Optional<Area> fromNBT(@Nullable CompoundTag tag) {
        if (tag == null || tag.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(new Area(
                tag.getInt("minX"), tag.getInt("minY"), tag.getInt("minZ"),
                tag.getInt("maxX"), tag.getInt("maxY"), tag.getInt("maxZ"),
                Direction.byName(tag.getString("direction"))
            ));
        }
    }

    @NotNull
    public static Stream<BlockPos> getFramePosStream(@NotNull Area area) {
        return Stream.of(
            makeSquare(area, area.minY()),
            makePole(area, area.minY() + 1, area.maxY()),
            makeSquare(area, area.maxY())
        ).flatMap(Function.identity());
    }

    static IntStream to(int first, int endInclusive) {
        if (first < endInclusive) {
            return IntStream.rangeClosed(first, endInclusive);
        } else if (first > endInclusive) {
            return IntStream.iterate(first, i -> i >= endInclusive, i -> i - 1);
        } else { // first == endInclusive
            return IntStream.of(first);
        }
    }

    static Stream<BlockPos> makeSquare(Area area, int y) {
        return Stream.of(
            to(area.minX(), area.maxX()).mapToObj(x -> new BlockPos(x, y, area.minZ())), // minX -> maxX, minZ
            to(area.minZ(), area.maxZ()).mapToObj(z -> new BlockPos(area.maxX(), y, z)), // maxX, minZ -> maxZ
            to(area.maxX(), area.minX()).mapToObj(x -> new BlockPos(x, y, area.maxZ())), // maxX -> minX, maxZ
            to(area.maxZ(), area.minZ()).mapToObj(z -> new BlockPos(area.minX(), y, z)) // minX, maxZ -> minZ
        ).flatMap(Function.identity());
    }

    static Stream<BlockPos> makePole(Area area, int yMin, int yMax) {
        return to(yMin, yMax).boxed().flatMap(y ->
            Stream.of(
                new BlockPos(area.minX(), y, area.minZ()),
                new BlockPos(area.maxX(), y, area.minZ()),
                new BlockPos(area.maxX(), y, area.maxZ()),
                new BlockPos(area.minX(), y, area.maxZ())));
    }

    public List<ChunkPos> getChunkPosList() {
        List<ChunkPos> posList = new ArrayList<>();
        for (int x = this.minX / 16; x <= this.maxX / 16; x++) {
            for (int z = this.minZ / 16; z <= this.maxZ / 16; z++) {
                posList.add(new ChunkPos(x, z));
            }
        }
        return posList;
    }
}
