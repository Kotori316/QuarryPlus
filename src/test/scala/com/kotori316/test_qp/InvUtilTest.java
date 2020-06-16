package com.kotori316.test_qp;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.yogpc.qp.compat.InvUtils;
import net.minecraft.util.Direction;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import static net.minecraft.util.Direction.DOWN;
import static net.minecraft.util.Direction.EAST;
import static net.minecraft.util.Direction.NORTH;
import static net.minecraft.util.Direction.SOUTH;
import static net.minecraft.util.Direction.UP;
import static net.minecraft.util.Direction.WEST;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

class InvUtilTest {
    @Test
    void directionOrderTest() {
        List<Pair<Direction, List<Direction>>> testCases = Arrays.asList(
            Pair.of(NORTH, Arrays.asList(NORTH, EAST, SOUTH, WEST, UP, DOWN)),
            Pair.of(SOUTH, Arrays.asList(SOUTH, WEST, NORTH, EAST, UP, DOWN)),
            Pair.of(EAST, Arrays.asList(EAST, SOUTH, WEST, NORTH, UP, DOWN)),
            Pair.of(WEST, Arrays.asList(WEST, NORTH, EAST, SOUTH, UP, DOWN)),
            Pair.of(UP, Arrays.asList(UP, NORTH, EAST, SOUTH, WEST, DOWN)),
            Pair.of(DOWN, Arrays.asList(DOWN, NORTH, EAST, SOUTH, WEST, UP)),
            Pair.of(null, Arrays.asList(DOWN, UP, NORTH, SOUTH, WEST, EAST))
        );
        assertAll(testCases.stream().map(p -> () ->
            assertIterableEquals(p.getValue(), InvUtils.clockwiseDirections(p.getKey()).collect(Collectors.toList()), "Direction " + p.getKey())));
    }
}
