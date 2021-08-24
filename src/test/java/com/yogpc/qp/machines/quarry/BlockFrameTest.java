package com.yogpc.qp.machines.quarry;

import java.util.List;

import com.yogpc.qp.QuarryPlusTest;
import net.minecraft.util.math.Vec3i;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class BlockFrameTest extends QuarryPlusTest {
    @Test
    void direction8() {
        assertEquals(8, BlockFrame.Direction8.DIRECTIONS.size());
        assertEquals(8, vectorLength().size());
    }

    @ParameterizedTest
    @MethodSource
    void vectorLength(BlockFrame.Direction8 direction8) {
        var vec = direction8.vec();
        var length = vec.getManhattanDistance(Vec3i.ZERO);
        if (length != 1 && length != 2) {
            fail("Invalid vector length: " + length);
        }
    }

    static List<BlockFrame.Direction8> vectorLength() {
        return BlockFrame.Direction8.DIRECTIONS;
    }
}
