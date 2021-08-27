package com.yogpc.qp.machines;

import java.util.List;

import com.yogpc.qp.QuarryPlusTest;
import net.minecraft.util.math.Vec3i;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

class Direction8Test extends QuarryPlusTest {
    @Test
    void direction8() {
        assertEquals(26, Direction8.DIRECTIONS.size());
        assertEquals(26, vectorLength().size());
    }

    @ParameterizedTest
    @MethodSource
    void vectorLength(Direction8 direction8) {
        var vec = direction8.vec();
        var length = vec.getManhattanDistance(Vec3i.ZERO);
        if (length > 3) {
            fail("Invalid vector length: " + length);
        }
    }

    @Test
    void noDuplication() {
        assertEquals(26, Direction8.DIRECTIONS.stream().distinct().count());
    }

    @Test
    void disallowModification() {
        assertThrows(UnsupportedOperationException.class, () -> Direction8.DIRECTIONS.add(new Direction8(Vec3i.ZERO)));
    }

    static List<Direction8> vectorLength() {
        return Direction8.DIRECTIONS;
    }
}
