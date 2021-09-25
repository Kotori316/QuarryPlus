package com.yogpc.qp.machines.advquarry;

import java.util.List;
import java.util.stream.StreamSupport;

import com.yogpc.qp.QuarryPlusTest;
import com.yogpc.qp.machines.Area;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

class TargetIteratorTest extends QuarryPlusTest {
    @Nested
    class NorthTest {
        // Dig area: -1005, 91 -> -1002, 92
        final Area area = new Area(-1006, 5, 90, -1001, 5, 93, Direction.NORTH);

        @Test
        void init() {
            var iterator = TargetIterator.of(area);
            assertEquals(new TargetIterator.XZPair(-1002, 91), iterator.peek());
        }

        @Test
        void checkAll() {
            Iterable<TargetIterator.XZPair> iterable = () -> TargetIterator.of(area);
            var allElements = StreamSupport.stream(iterable.spliterator(), false).toList();
            assertIterableEquals(List.of(
                new TargetIterator.XZPair(-1002, 91),
                new TargetIterator.XZPair(-1002, 92),
                new TargetIterator.XZPair(-1003, 91),
                new TargetIterator.XZPair(-1003, 92),
                new TargetIterator.XZPair(-1004, 91),
                new TargetIterator.XZPair(-1004, 92),
                new TargetIterator.XZPair(-1005, 91),
                new TargetIterator.XZPair(-1005, 92)
            ), allElements);
        }
    }

    @Nested
    class SouthTest {
        // Dig area: -1005, 91 -> -1002, 92
        final Area area = new Area(-1006, 5, 90, -1001, 5, 93, Direction.SOUTH);

        @Test
        void init() {
            var iterator = TargetIterator.of(area);
            assertEquals(new TargetIterator.XZPair(-1005, 92), iterator.peek());
        }

        @Test
        void checkAll() {
            Iterable<TargetIterator.XZPair> iterable = () -> TargetIterator.of(area);
            var allElements = StreamSupport.stream(iterable.spliterator(), false).toList();
            assertIterableEquals(List.of(
                new TargetIterator.XZPair(-1005, 92),
                new TargetIterator.XZPair(-1005, 91),
                new TargetIterator.XZPair(-1004, 92),
                new TargetIterator.XZPair(-1004, 91),
                new TargetIterator.XZPair(-1003, 92),
                new TargetIterator.XZPair(-1003, 91),
                new TargetIterator.XZPair(-1002, 92),
                new TargetIterator.XZPair(-1002, 91)
            ), allElements);
        }
    }

    @Nested
    class WestTest {
        // Dig area: -1005, 91 -> -1002, 92
        final Area area = new Area(-1006, 5, 90, -1001, 5, 93, Direction.WEST);

        @Test
        void init() {
            var iterator = TargetIterator.of(area);
            assertEquals(new TargetIterator.XZPair(-1005, 91), iterator.peek());
        }

        @Test
        void checkAll() {
            Iterable<TargetIterator.XZPair> iterable = () -> TargetIterator.of(area);
            var allElements = StreamSupport.stream(iterable.spliterator(), false).toList();
            assertIterableEquals(List.of(
                new TargetIterator.XZPair(-1005, 91),
                new TargetIterator.XZPair(-1004, 91),
                new TargetIterator.XZPair(-1003, 91),
                new TargetIterator.XZPair(-1002, 91),
                new TargetIterator.XZPair(-1005, 92),
                new TargetIterator.XZPair(-1004, 92),
                new TargetIterator.XZPair(-1003, 92),
                new TargetIterator.XZPair(-1002, 92)
            ), allElements);
        }
    }

    @Nested
    class EastTest {
        // Dig area: -1005, 91 -> -1002, 92
        final Area area = new Area(-1006, 5, 90, -1001, 5, 93, Direction.EAST);

        @Test
        void init() {
            var iterator = TargetIterator.of(area);
            assertEquals(new TargetIterator.XZPair(-1002, 92), iterator.peek());
        }

        @Test
        void checkAll() {
            Iterable<TargetIterator.XZPair> iterable = () -> TargetIterator.of(area);
            var allElements = StreamSupport.stream(iterable.spliterator(), false).toList();
            assertIterableEquals(List.of(
                new TargetIterator.XZPair(-1002, 92),
                new TargetIterator.XZPair(-1003, 92),
                new TargetIterator.XZPair(-1004, 92),
                new TargetIterator.XZPair(-1005, 92),
                new TargetIterator.XZPair(-1002, 91),
                new TargetIterator.XZPair(-1003, 91),
                new TargetIterator.XZPair(-1004, 91),
                new TargetIterator.XZPair(-1005, 91)
            ), allElements);
        }
    }
}
