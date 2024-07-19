package com.yogpc.qp.machine.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;

import java.util.List;
import java.util.function.Predicate;

public record Direction26(Vec3i vec) {

    public static final List<Direction26> DIRECTIONS;

    static {
        DIRECTIONS = BlockPos.betweenClosedStream(-1, -1, -1, 1, 1, 1)
            .map(BlockPos::immutable)
            .filter(Predicate.isEqual(BlockPos.ZERO).negate())
            .map(Direction26::new)
            .toList();
    }
}
