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
    private static final TargetIterator.XZPair ZERO = new TargetIterator.XZPair(0, 0);

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
                new TargetIterator.XZPair(2, -1),
                new TargetIterator.XZPair(2, 0),
                new TargetIterator.XZPair(2, 1),
                new TargetIterator.XZPair(3, -1),
                new TargetIterator.XZPair(3, 0),
                new TargetIterator.XZPair(3, 1),
                new TargetIterator.XZPair(4, -1),
                new TargetIterator.XZPair(4, 0),
                new TargetIterator.XZPair(4, 1)
            ), adjacent
        );
    }

    @Test
    void adjacentTest3() {
        var adjacent = CircleGenerator.testAdjacent(ZERO, 3, Math.PI / 2);
        assertEquals(
            Set.of(
                new TargetIterator.XZPair(-1, 2),
                new TargetIterator.XZPair(-1, 3),
                new TargetIterator.XZPair(-1, 4),
                new TargetIterator.XZPair(0, 2),
                new TargetIterator.XZPair(0, 3),
                new TargetIterator.XZPair(0, 4),
                new TargetIterator.XZPair(1, 2),
                new TargetIterator.XZPair(1, 3),
                new TargetIterator.XZPair(1, 4)
            ), adjacent
        );
    }

    @Test
    @DisplayName("Center: (0, 0), Radius: 4.5")
    void makeCircle1() {
        var ans = List.of(
            new TargetIterator.XZPair(-4, -1),
            new TargetIterator.XZPair(-3, -2),
            new TargetIterator.XZPair(-3, -3),
            new TargetIterator.XZPair(-2, -3),
            new TargetIterator.XZPair(-1, -4),
            new TargetIterator.XZPair(0, -4),
            new TargetIterator.XZPair(1, -4),
            new TargetIterator.XZPair(2, -3),
            new TargetIterator.XZPair(3, -3),
            new TargetIterator.XZPair(3, -2),
            new TargetIterator.XZPair(4, -1),
            new TargetIterator.XZPair(4, 0),
            new TargetIterator.XZPair(4, 1),
            new TargetIterator.XZPair(3, 2),
            new TargetIterator.XZPair(3, 3),
            new TargetIterator.XZPair(2, 3),
            new TargetIterator.XZPair(1, 4),
            new TargetIterator.XZPair(0, 4),
            new TargetIterator.XZPair(-1, 4),
            new TargetIterator.XZPair(-2, 3),
            new TargetIterator.XZPair(-3, 3),
            new TargetIterator.XZPair(-3, 2),
            new TargetIterator.XZPair(-4, 1),
            new TargetIterator.XZPair(-4, 0)
        );
        assertIterableEquals(ans, CircleGenerator.testCircle(
            ZERO, 9
        ));
    }

    @Test
    @DisplayName("Center: (0, 0), Radius: 5")
    void makeCircle2() {
        var ans = List.of(
            new TargetIterator.XZPair(-5, -1),
            new TargetIterator.XZPair(-5, -2),
            new TargetIterator.XZPair(-4, -3),
            new TargetIterator.XZPair(-3, -4),
            new TargetIterator.XZPair(-2, -5),
            new TargetIterator.XZPair(-1, -5),
            new TargetIterator.XZPair(0, -5),
            new TargetIterator.XZPair(1, -5),
            new TargetIterator.XZPair(2, -4),
            new TargetIterator.XZPair(3, -3),
            new TargetIterator.XZPair(4, -2),
            new TargetIterator.XZPair(4, -1),
            new TargetIterator.XZPair(4, 0),
            new TargetIterator.XZPair(4, 1),
            new TargetIterator.XZPair(3, 2),
            new TargetIterator.XZPair(2, 3),
            new TargetIterator.XZPair(1, 4),
            new TargetIterator.XZPair(0, 4),
            new TargetIterator.XZPair(-1, 4),
            new TargetIterator.XZPair(-2, 4),
            new TargetIterator.XZPair(-3, 3),
            new TargetIterator.XZPair(-4, 2),
            new TargetIterator.XZPair(-5, 1),
            new TargetIterator.XZPair(-5, 0)
        );
        assertIterableEquals(ans, CircleGenerator.testCircle(
            ZERO, 10
        ));
    }

    @Test
    void makeCircle3() {
        var ans = List.of(
            new TargetIterator.XZPair(-6, 6),
            new TargetIterator.XZPair(-6, 5),
            new TargetIterator.XZPair(-6, 4),
            new TargetIterator.XZPair(-5, 3),
            new TargetIterator.XZPair(-5, 2),
            new TargetIterator.XZPair(-4, 1),
            new TargetIterator.XZPair(-3, 0),
            new TargetIterator.XZPair(-2, -1),
            new TargetIterator.XZPair(-1, -2),
            new TargetIterator.XZPair(0, -2),
            new TargetIterator.XZPair(1, -3),
            new TargetIterator.XZPair(2, -3),
            new TargetIterator.XZPair(3, -3),
            new TargetIterator.XZPair(4, -3),
            new TargetIterator.XZPair(5, -3),
            new TargetIterator.XZPair(6, -3),
            new TargetIterator.XZPair(7, -2),
            new TargetIterator.XZPair(8, -2),
            new TargetIterator.XZPair(9, -1),
            new TargetIterator.XZPair(10, 0),
            new TargetIterator.XZPair(11, 1),
            new TargetIterator.XZPair(12, 2),
            new TargetIterator.XZPair(12, 3),
            new TargetIterator.XZPair(13, 4),
            new TargetIterator.XZPair(13, 5),
            new TargetIterator.XZPair(13, 6),
            new TargetIterator.XZPair(13, 7),
            new TargetIterator.XZPair(13, 8),
            new TargetIterator.XZPair(13, 9),
            new TargetIterator.XZPair(12, 10),
            new TargetIterator.XZPair(12, 11),
            new TargetIterator.XZPair(11, 12),
            new TargetIterator.XZPair(10, 13),
            new TargetIterator.XZPair(9, 14),
            new TargetIterator.XZPair(8, 15),
            new TargetIterator.XZPair(7, 15),
            new TargetIterator.XZPair(6, 16),
            new TargetIterator.XZPair(5, 16),
            new TargetIterator.XZPair(4, 16),
            new TargetIterator.XZPair(3, 16),
            new TargetIterator.XZPair(2, 16),
            new TargetIterator.XZPair(1, 16),
            new TargetIterator.XZPair(0, 15),
            new TargetIterator.XZPair(-1, 15),
            new TargetIterator.XZPair(-2, 14),
            new TargetIterator.XZPair(-3, 13),
            new TargetIterator.XZPair(-4, 12),
            new TargetIterator.XZPair(-5, 11),
            new TargetIterator.XZPair(-5, 10),
            new TargetIterator.XZPair(-6, 9),
            new TargetIterator.XZPair(-6, 8),
            new TargetIterator.XZPair(-6, 7)
        );
        assertIterableEquals(ans, CircleGenerator.testCircle(new TargetIterator.XZPair(4, 7), 20));
    }
}
