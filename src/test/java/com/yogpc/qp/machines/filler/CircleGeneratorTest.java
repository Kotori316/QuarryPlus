package com.yogpc.qp.machines.filler;

import java.util.List;
import java.util.Set;

import com.yogpc.qp.QuarryPlusTest;
import com.yogpc.qp.machines.TargetIterator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

class CircleGeneratorTest extends QuarryPlusTest {
    private static TargetIterator.XZPair xz(int x, int z) {
        return new TargetIterator.XZPair(x, z);
    }

    private static final TargetIterator.XZPair ZERO = xz(0, 0);

    @Test
    void adjacentTest1() {
        var adjacent = CircleGenerator.testAdjacent(ZERO, 0, 0);
        assertEquals(9, adjacent.size());
    }

    @Test
    void adjacentTest2() {
        var adjacent = CircleGenerator.testAdjacent(ZERO, 3, 0);
        assertEquals(
            Set.of(
                xz(2, -1),
                xz(2, 0),
                xz(2, 1),
                xz(3, -1),
                xz(3, 0),
                xz(3, 1),
                xz(4, -1),
                xz(4, 0),
                xz(4, 1)
            ), adjacent
        );
    }

    @Test
    void adjacentTest3() {
        var adjacent = CircleGenerator.testAdjacent(ZERO, 3, Math.PI / 2);
        assertEquals(
            Set.of(
                xz(-1, 2),
                xz(-1, 3),
                xz(-1, 4),
                xz(0, 2),
                xz(0, 3),
                xz(0, 4),
                xz(1, 2),
                xz(1, 3),
                xz(1, 4)
            ), adjacent
        );
    }

    @Test
    @DisplayName("Center: (0, 0), Radius: 4.5")
    void makeCircle1() {
        var ans = List.of(
            xz(-4, -1),
            xz(-3, -2),
            xz(-3, -3),
            xz(-2, -3),
            xz(-1, -4),
            xz(0, -4),
            xz(1, -4),
            xz(2, -3),
            xz(3, -3),
            xz(3, -2),
            xz(4, -1),
            xz(4, 0),
            xz(4, 1),
            xz(3, 2),
            xz(3, 3),
            xz(2, 3),
            xz(1, 4),
            xz(0, 4),
            xz(-1, 4),
            xz(-2, 3),
            xz(-3, 3),
            xz(-3, 2),
            xz(-4, 1),
            xz(-4, 0)
        );
        assertIterableEquals(ans, CircleGenerator.testCircle(
            ZERO, 9
        ));
    }

    @Test
    @DisplayName("Center: (0, 0), Radius: 5")
    void makeCircle2() {
        var ans = List.of(
            xz(-5, -1),
            xz(-5, -2),
            xz(-4, -3),
            xz(-3, -4),
            xz(-2, -5),
            xz(-1, -5),
            xz(0, -5),
            xz(1, -5),
            xz(2, -4),
            xz(3, -3),
            xz(4, -2),
            xz(4, -1),
            xz(4, 0),
            xz(4, 1),
            xz(3, 2),
            xz(2, 3),
            xz(1, 4),
            xz(0, 4),
            xz(-1, 4),
            xz(-2, 4),
            xz(-3, 3),
            xz(-4, 2),
            xz(-5, 1),
            xz(-5, 0)
        );
        assertIterableEquals(ans, CircleGenerator.testCircle(
            ZERO, 10
        ));
    }

    @Test
    void makeCircle3() {
        var ans = List.of(
            xz(-6, 6),
            xz(-6, 5),
            xz(-6, 4),
            xz(-5, 3),
            xz(-5, 2),
            xz(-4, 1),
            xz(-3, 0),
            xz(-2, -1),
            xz(-1, -2),
            xz(0, -2),
            xz(1, -3),
            xz(2, -3),
            xz(3, -3),
            xz(4, -3),
            xz(5, -3),
            xz(6, -3),
            xz(7, -2),
            xz(8, -2),
            xz(9, -1),
            xz(10, 0),
            xz(11, 1),
            xz(12, 2),
            xz(12, 3),
            xz(13, 4),
            xz(13, 5),
            xz(13, 6),
            xz(13, 7),
            xz(13, 8),
            xz(13, 9),
            xz(12, 10),
            xz(12, 11),
            xz(11, 12),
            xz(10, 13),
            xz(9, 14),
            xz(8, 15),
            xz(7, 15),
            xz(6, 16),
            xz(5, 16),
            xz(4, 16),
            xz(3, 16),
            xz(2, 16),
            xz(1, 16),
            xz(0, 15),
            xz(-1, 15),
            xz(-2, 14),
            xz(-3, 13),
            xz(-4, 12),
            xz(-5, 11),
            xz(-5, 10),
            xz(-6, 9),
            xz(-6, 8),
            xz(-6, 7)
        );
        assertIterableEquals(ans, CircleGenerator.testCircle(xz(4, 7), 20));
    }
}
