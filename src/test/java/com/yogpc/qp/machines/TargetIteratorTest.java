package com.yogpc.qp.machines;

import java.time.Duration;
import java.util.List;
import java.util.stream.StreamSupport;

import com.yogpc.qp.QuarryPlusTest;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(QuarryPlusTest.class)
class TargetIteratorTest {
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
        void head() {
            var iterator = TargetIterator.of(area);
            assertEquals(new TargetIterator.XZPair(-1002, 91), iterator.head());
            iterator.next();
            assertEquals(new TargetIterator.XZPair(-1002, 91), iterator.head());
        }

        @Test
        void reset() {
            var iterator = TargetIterator.of(area);
            iterator.next();
            iterator.next();
            iterator.reset();
            assertEquals(iterator.head(), iterator.peek());
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
        void head() {
            var iterator = TargetIterator.of(area);
            assertEquals(new TargetIterator.XZPair(-1005, 92), iterator.head());
            iterator.next();
            assertEquals(new TargetIterator.XZPair(-1005, 92), iterator.head());
        }

        @Test
        void reset() {
            var iterator = TargetIterator.of(area);
            iterator.next();
            iterator.next();
            iterator.reset();
            assertEquals(iterator.head(), iterator.peek());
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
        void head() {
            var iterator = TargetIterator.of(area);
            assertEquals(new TargetIterator.XZPair(-1005, 91), iterator.head());
            iterator.next();
            assertEquals(new TargetIterator.XZPair(-1005, 91), iterator.head());
        }

        @Test
        void reset() {
            var iterator = TargetIterator.of(area);
            iterator.next();
            iterator.next();
            iterator.reset();
            assertEquals(iterator.head(), iterator.peek());
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
        void head() {
            var iterator = TargetIterator.of(area);
            assertEquals(new TargetIterator.XZPair(-1002, 92), iterator.head());
            iterator.next();
            assertEquals(new TargetIterator.XZPair(-1002, 92), iterator.head());
        }

        @Test
        void reset() {
            var iterator = TargetIterator.of(area);
            iterator.next();
            iterator.next();
            iterator.reset();
            assertEquals(iterator.head(), iterator.peek());
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

    @Nested
    class OtherTests {
        @Test
        @DisplayName("One for each y")
        void one() {
            var area = new Area(3, 10, 6, 5, 14, 8, Direction.NORTH);
            Iterable<TargetIterator.XZPair> iterable = () -> TargetIterator.of(area);
            var allElements = StreamSupport.stream(iterable.spliterator(), false).toList();
            assertEquals(List.of(new TargetIterator.XZPair(4, 7)), allElements);
        }
    }

    @Nested
    class ChunkByChunkTest {
        @Test
        void noInfiniteLoop1() {
            // (0, 0) -> (5, 5), end exclusive
            Area area = new Area(0, 0, 0, 4, 0, 4, Direction.NORTH).shrink(-1, 0, -1);
            Iterable<TargetIterator.XZPair> iterable = () -> TargetIterator.of(area, true);
            var list = assertTimeout(Duration.ofSeconds(3), () -> StreamSupport.stream(iterable.spliterator(), false).toList());
            assertEquals(25, list.size());
        }

        @Test
        void noInfiniteLoop2() {
            // (-18, -20) -> (26, 31), end exclusive
            // Parameter -18, 0, -20, 25, 0, 30 is inclusive
            Area area = new Area(-18, 0, -20, 25, 0, 30, Direction.NORTH).shrink(-1, 0, -1);
            Iterable<TargetIterator.XZPair> iterable = () -> TargetIterator.of(area, true);
            var list = assertTimeout(Duration.ofSeconds(3), () -> StreamSupport.stream(iterable.spliterator(), false).toList());
            assertEquals(2244, list.size());
            assertEquals(new TargetIterator.XZPair(-18, -20), list.get(0));
            assertEquals(new TargetIterator.XZPair(25, 30), list.get(list.size() - 1));
        }

        @Test
        void inSameChunk() {
            // (1, 1) -> (5, 5), end exclusive
            // Parameter: (0, 0, 0) -> (5, 0, 5)
            Area area = new Area(0, 0, 0, 5, 0, 5, Direction.NORTH);
            Iterable<TargetIterator.XZPair> iterable = () -> TargetIterator.of(area, true);
            var list = assertTimeout(Duration.ofSeconds(3), () -> StreamSupport.stream(iterable.spliterator(), false).toList());
            var expected = List.of(
                new TargetIterator.XZPair(1, 1),
                new TargetIterator.XZPair(2, 1),
                new TargetIterator.XZPair(3, 1),
                new TargetIterator.XZPair(4, 1),
                new TargetIterator.XZPair(1, 2),
                new TargetIterator.XZPair(2, 2),
                new TargetIterator.XZPair(3, 2),
                new TargetIterator.XZPair(4, 2),
                new TargetIterator.XZPair(1, 3),
                new TargetIterator.XZPair(2, 3),
                new TargetIterator.XZPair(3, 3),
                new TargetIterator.XZPair(4, 3),
                new TargetIterator.XZPair(1, 4),
                new TargetIterator.XZPair(2, 4),
                new TargetIterator.XZPair(3, 4),
                new TargetIterator.XZPair(4, 4)
            );
            assertIterableEquals(expected, list);
        }

        @Test
        void crossXZChunk() {
            // (14, 14), (19, 19), end exclusive
            // Parameter: (13, 0, 13) -> (19, 0, 19)
            Area area = new Area(13, 0, 13, 19, 0, 19, Direction.NORTH);
            Iterable<TargetIterator.XZPair> iterable = () -> TargetIterator.of(area, true);
            var list = assertTimeout(Duration.ofSeconds(3), () -> StreamSupport.stream(iterable.spliterator(), false).toList());
            var expected = List.of(
                new TargetIterator.XZPair(14, 14),
                new TargetIterator.XZPair(15, 14),
                new TargetIterator.XZPair(14, 15),
                new TargetIterator.XZPair(15, 15),
                new TargetIterator.XZPair(16, 14),
                new TargetIterator.XZPair(17, 14),
                new TargetIterator.XZPair(18, 14),
                new TargetIterator.XZPair(16, 15),
                new TargetIterator.XZPair(17, 15),
                new TargetIterator.XZPair(18, 15),
                new TargetIterator.XZPair(14, 16),
                new TargetIterator.XZPair(15, 16),
                new TargetIterator.XZPair(14, 17),
                new TargetIterator.XZPair(15, 17),
                new TargetIterator.XZPair(14, 18),
                new TargetIterator.XZPair(15, 18),
                new TargetIterator.XZPair(16, 16),
                new TargetIterator.XZPair(17, 16),
                new TargetIterator.XZPair(18, 16),
                new TargetIterator.XZPair(16, 17),
                new TargetIterator.XZPair(17, 17),
                new TargetIterator.XZPair(18, 17),
                new TargetIterator.XZPair(16, 18),
                new TargetIterator.XZPair(17, 18),
                new TargetIterator.XZPair(18, 18)
            );
            assertIterableEquals(expected, list);
        }

        @Test
        @DisplayName("crossXZChunk_setCurrent")
        void crossXZChunkSetCurrent() {
            // (14, 14), (19, 19), end exclusive
            // Parameter: (13, 0, 13) -> (19, 0, 19)
            Area area = new Area(13, 0, 13, 19, 0, 19, Direction.NORTH);
            Iterable<TargetIterator.XZPair> iterable = () -> {
                var t = TargetIterator.of(area, true);
                t.setCurrent(new TargetIterator.XZPair(15, 18));
                return t;
            };
            var expected = List.of(
                // Current
                new TargetIterator.XZPair(15, 18),
                new TargetIterator.XZPair(16, 16),
                new TargetIterator.XZPair(17, 16),
                new TargetIterator.XZPair(18, 16),
                new TargetIterator.XZPair(16, 17),
                new TargetIterator.XZPair(17, 17),
                new TargetIterator.XZPair(18, 17),
                new TargetIterator.XZPair(16, 18),
                new TargetIterator.XZPair(17, 18),
                new TargetIterator.XZPair(18, 18)
            );
            var list = assertTimeout(Duration.ofSeconds(3), () -> StreamSupport.stream(iterable.spliterator(), false).toList());
            assertIterableEquals(expected, list);
        }

        @Test
        void crossXChunk() {
            // (14, 10), (19, 13), end exclusive
            // Parameter: (13, 0, 9) -> (19, 0, 13)
            Area area = new Area(13, 0, 9, 19, 0, 13, Direction.NORTH);
            Iterable<TargetIterator.XZPair> iterable = () -> TargetIterator.of(area, true);
            var list = assertTimeout(Duration.ofSeconds(3), () -> StreamSupport.stream(iterable.spliterator(), false).toList());
            var expected = List.of(
                new TargetIterator.XZPair(14, 10),
                new TargetIterator.XZPair(15, 10),
                new TargetIterator.XZPair(14, 11),
                new TargetIterator.XZPair(15, 11),
                new TargetIterator.XZPair(14, 12),
                new TargetIterator.XZPair(15, 12),
                new TargetIterator.XZPair(16, 10),
                new TargetIterator.XZPair(17, 10),
                new TargetIterator.XZPair(18, 10),
                new TargetIterator.XZPair(16, 11),
                new TargetIterator.XZPair(17, 11),
                new TargetIterator.XZPair(18, 11),
                new TargetIterator.XZPair(16, 12),
                new TargetIterator.XZPair(17, 12),
                new TargetIterator.XZPair(18, 12)
            );
            assertIterableEquals(expected, list);
        }

        @Test
        void crossZChunk() {
            // (-12, -34), (-9, -30), end exclusive
            // Parameter: (-13, 0, -35) -> (-9, 0, -30)
            Area area = new Area(-13, 0, -35, -9, 0, -30, Direction.NORTH);
            Iterable<TargetIterator.XZPair> iterable = () -> TargetIterator.of(area, true);
            var list = assertTimeout(Duration.ofSeconds(3), () -> StreamSupport.stream(iterable.spliterator(), false).toList());
            var expected = List.of(
                new TargetIterator.XZPair(-12, -34),
                new TargetIterator.XZPair(-11, -34),
                new TargetIterator.XZPair(-10, -34),
                new TargetIterator.XZPair(-12, -33),
                new TargetIterator.XZPair(-11, -33),
                new TargetIterator.XZPair(-10, -33),
                new TargetIterator.XZPair(-12, -32),
                new TargetIterator.XZPair(-11, -32),
                new TargetIterator.XZPair(-10, -32),
                new TargetIterator.XZPair(-12, -31),
                new TargetIterator.XZPair(-11, -31),
                new TargetIterator.XZPair(-10, -31)
            );
            assertIterableEquals(expected, list);
        }

        @Test
        @DisplayName("(0, 0) -> (16, 16), end exclusive")
        void oneChunk1() {
            // (0, 0) -> (16, 16), end exclusive
            Area area = new Area(-1, 0, -1, 16, 0, 16, Direction.NORTH);
            Iterable<TargetIterator.XZPair> iterable = () -> TargetIterator.of(area, true);
            var list = assertTimeout(Duration.ofSeconds(3), () -> StreamSupport.stream(iterable.spliterator(), false).toList());
            assertAll(
                () -> assertEquals(new TargetIterator.XZPair(0, 0), list.get(0)),
                () -> assertFalse(list.contains(new TargetIterator.XZPair(16, 16))),
                () -> assertEquals(256, list.size())
            );
        }

        @Test
        @DisplayName("(0, 0) -> (15, 15), end exclusive")
        void oneChunk2() {
            // (0, 0) -> (15, 15), end exclusive
            Area area = new Area(-1, 0, -1, 15, 0, 15, Direction.NORTH);
            Iterable<TargetIterator.XZPair> iterable = () -> TargetIterator.of(area, true);
            var list = assertTimeout(Duration.ofSeconds(3), () -> StreamSupport.stream(iterable.spliterator(), false).toList());
            assertAll(
                () -> assertEquals(new TargetIterator.XZPair(0, 0), list.get(0)),
                () -> assertFalse(list.contains(new TargetIterator.XZPair(15, 15))),
                () -> assertEquals(225, list.size())
            );
        }

        @Test
        @DisplayName("(0, 0) -> (17, 17), end exclusive")
        void oneChunk3() {
            // (0, 0) -> (17, 17), end exclusive
            Area area = new Area(-1, 0, -1, 17, 0, 17, Direction.NORTH);
            Iterable<TargetIterator.XZPair> iterable = () -> TargetIterator.of(area, true);
            var list = assertTimeout(Duration.ofSeconds(3), () -> StreamSupport.stream(iterable.spliterator(), false).toList());
            assertAll(
                () -> assertEquals(new TargetIterator.XZPair(0, 0), list.get(0)),
                () -> assertTrue(list.contains(new TargetIterator.XZPair(16, 16))),
                () -> assertFalse(list.contains(new TargetIterator.XZPair(17, 17))),
                () -> assertEquals(289, list.size())
            );
        }
    }
}
