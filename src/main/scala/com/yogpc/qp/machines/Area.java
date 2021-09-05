package com.yogpc.qp.machines;

import java.io.Serial;
import java.io.Serializable;
import java.util.Optional;

import javax.annotation.Nullable;
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
}
