package com.yogpc.qp.machines;

import java.util.List;
import java.util.function.Predicate;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public record Direction8(Vec3i vec) {

    public static final List<Direction8> DIRECTIONS;

    static {
        DIRECTIONS = BlockPos.stream(-1, -1, -1, 1, 1, 1)
            .map(BlockPos::toImmutable)
            .filter(Predicate.isEqual(BlockPos.ORIGIN).negate())
            .map(Direction8::new)
            .toList();
    }
}
