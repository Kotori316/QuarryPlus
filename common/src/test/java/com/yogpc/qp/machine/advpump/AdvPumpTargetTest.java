package com.yogpc.qp.machine.advpump;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AdvPumpTargetTest {
    @Test
    void inRangeIncludesCenter() {
        var center = new BlockPos(0, 64, 0);
        var predicate = AdvPumpTarget.inRangePredicate(center, 32);
        assertTrue(predicate.test(center));
    }

    @Test
    void inRangeIsCircularNotSquare() {
        var center = new BlockPos(0, 64, 0);
        var predicate = AdvPumpTarget.inRangePredicate(center, 32);
        // Corner of the bounding square is farther than 32 in Euclidean distance, so it must be excluded
        // even though both axis offsets individually are within 32.
        assertFalse(predicate.test(center.offset(31, 0, 31)));
        assertTrue(predicate.test(center.offset(31, 0, 0)));
    }

    @Test
    void inRangeIgnoresY() {
        var center = new BlockPos(0, 64, 0);
        var predicate = AdvPumpTarget.inRangePredicate(center, 32);
        assertTrue(predicate.test(center.offset(0, 1000, 0)));
    }

    @Test
    void inRangeExcludesExactBoundary() {
        var center = new BlockPos(0, 64, 0);
        var predicate = AdvPumpTarget.inRangePredicate(center, 32);
        assertFalse(predicate.test(center.offset(32, 0, 0)));
        assertTrue(predicate.test(center.offset(31, 0, 0)));
    }

    @Test
    void areaSizeHintScalesWithRangeSquared() {
        assertEquals((int) Math.PI, AdvPumpTarget.areaSizeHint(1));
        assertTrue(AdvPumpTarget.areaSizeHint(64) > AdvPumpTarget.areaSizeHint(32));
    }
}
