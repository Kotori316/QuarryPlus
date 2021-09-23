package com.yogpc.qp.machines;

import java.io.Serial;
import java.io.Serializable;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;

public record Area(int minX, int minY, int minZ, int maxX, int maxY, int maxZ,
                   Direction direction) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

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

    public boolean isInAreaIgnoreY(Vec3i vec3i) {
        return minX < vec3i.getX() && vec3i.getX() < maxX && minZ < vec3i.getZ() && vec3i.getZ() < maxZ;
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

    @Nonnull
    public static Stream<BlockPos> getFramePosStream(@Nonnull Area area) {
        return Stream.of(
            makeSquare(area, area.minY()),
            makePole(area, area.minY() + 1, area.maxY()),
            makeSquare(area, area.maxY())
        ).flatMap(Function.identity());
    }

    static Stream<BlockPos> makeSquare(Area area, int y) {
        return Stream.of(
            IntStream.rangeClosed(area.minX(), area.maxX()).mapToObj(x -> new BlockPos(x, y, area.minZ())), // minX -> maxX, minZ
            IntStream.rangeClosed(area.minZ(), area.maxZ()).mapToObj(z -> new BlockPos(area.maxX(), y, z)), // maxX, minZ -> maxZ
            IntStream.iterate(area.maxX(), x -> x >= area.minX(), x -> x - 1).mapToObj(x -> new BlockPos(x, y, area.maxZ())), // maxX -> minX, maxZ
            IntStream.iterate(area.maxZ(), z -> z >= area.minZ(), z -> z - 1).mapToObj(z -> new BlockPos(area.minX(), y, z)) // minX, maxZ -> minZ
        ).flatMap(Function.identity());
    }

    static Stream<BlockPos> makePole(Area area, int yMin, int yMax) {
        return IntStream.rangeClosed(yMin, yMax).boxed().flatMap(y ->
            Stream.of(
                new BlockPos(area.minX(), y, area.minZ()),
                new BlockPos(area.maxX(), y, area.minZ()),
                new BlockPos(area.maxX(), y, area.maxZ()),
                new BlockPos(area.minX(), y, area.maxZ())));
    }

}
