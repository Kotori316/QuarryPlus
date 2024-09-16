package com.yogpc.qp.machine.advquarry;

import com.yogpc.qp.machine.Area;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;

class AdvQuarryTargetTest {
    @Nested
    class NorthTest {
        // Dig area: -1005, 91 -> -1002, 92
        final Area area = new Area(-1006, 5, 90, -1001, 5, 93, Direction.NORTH);

        @Test
        void init() {
            var iterator = new AdvQuarryTarget.North(area);
            assertEquals(new BlockPos(-1002, 5, 91), iterator.next());
        }

        @Test
        void head() {
            var iterator = new AdvQuarryTarget.North(area);
            assertEquals(new BlockPos(-1002, 5, 91), iterator.next());
            iterator.next();
            assertEquals(new BlockPos(-1003, 5, 91), iterator.next());
        }

        @Test
        void reset() {
            var iterator = new AdvQuarryTarget.North(area);
            var head = iterator.next();
            iterator.next();
            iterator.setLastReturned(null);
            assertEquals(head, iterator.next());
        }

        @Test
        void checkAll() {
            Iterable<BlockPos> iterable = () -> new AdvQuarryTarget.North(area);
            var allElements = assertTimeout(Duration.ofSeconds(3), () ->
                StreamSupport.stream(iterable.spliterator(), false)
                    .map(BlockPos::immutable)
                    .toList());
            assertIterableEquals(List.of(
                new BlockPos(-1002, 5, 91),
                new BlockPos(-1002, 5, 92),
                new BlockPos(-1003, 5, 91),
                new BlockPos(-1003, 5, 92),
                new BlockPos(-1004, 5, 91),
                new BlockPos(-1004, 5, 92),
                new BlockPos(-1005, 5, 91),
                new BlockPos(-1005, 5, 92)
            ), allElements);
        }
    }


    @Nested
    class ChunkByChunkTest {
        static List<BlockPos> asList(Iterable<BlockPos> iterable) {
            return assertTimeout(Duration.ofSeconds(3), () ->
                StreamSupport.stream(iterable.spliterator(), false)
                    .map(BlockPos::immutable)
                    .toList()
            );
        }

        @Test
        void noInfiniteLoop1() {
            // (0, 0) -> (5, 5), end exclusive
            Area area = new Area(0, 0, 0, 4, 0, 4, Direction.NORTH).shrink(-1, 0, -1);
            var list = asList(() -> new AdvQuarryTarget.ChunkByChunk(area));
            assertEquals(25, list.size());
        }

        @Test
        void noInfiniteLoop2() {
            // (-18, -20) -> (26, 31), end exclusive
            // Parameter -18, 0, -20, 25, 0, 30 is inclusive
            Area area = new Area(-18, 0, -20, 25, 0, 30, Direction.NORTH).shrink(-1, 0, -1);
            var list = asList(() -> new AdvQuarryTarget.ChunkByChunk(area));
            assertEquals(2244, list.size());
            assertEquals(new BlockPos(-18, 0, -20), list.getFirst());
            assertEquals(new BlockPos(25, 0, 30), list.getLast());
        }

        @Test
        void inSameChunk() {
            // (1, 1) -> (5, 5), end exclusive
            // Parameter: (0, 0, 0) -> (5, 0, 5)
            Area area = new Area(0, 0, 0, 5, 0, 5, Direction.NORTH);
            var list = asList(() -> new AdvQuarryTarget.ChunkByChunk(area));
            var expected = List.of(
                new BlockPos(1, 0, 1),
                new BlockPos(2, 0, 1),
                new BlockPos(3, 0, 1),
                new BlockPos(4, 0, 1),
                new BlockPos(1, 0, 2),
                new BlockPos(2, 0, 2),
                new BlockPos(3, 0, 2),
                new BlockPos(4, 0, 2),
                new BlockPos(1, 0, 3),
                new BlockPos(2, 0, 3),
                new BlockPos(3, 0, 3),
                new BlockPos(4, 0, 3),
                new BlockPos(1, 0, 4),
                new BlockPos(2, 0, 4),
                new BlockPos(3, 0, 4),
                new BlockPos(4, 0, 4)
            );
            assertIterableEquals(expected, list);
        }

        @Test
        void crossXZChunk() {
            // (14, 14), (19, 19), end exclusive
            // Parameter: (13, 0, 13) -> (19, 0, 19)
            Area area = new Area(13, 0, 13, 19, 0, 19, Direction.NORTH);
            var list = asList(() -> new AdvQuarryTarget.ChunkByChunk(area));
            var expected = List.of(
                new BlockPos(14, 0, 14),
                new BlockPos(15, 0, 14),
                new BlockPos(14, 0, 15),
                new BlockPos(15, 0, 15),
                new BlockPos(16, 0, 14),
                new BlockPos(17, 0, 14),
                new BlockPos(18, 0, 14),
                new BlockPos(16, 0, 15),
                new BlockPos(17, 0, 15),
                new BlockPos(18, 0, 15),
                new BlockPos(14, 0, 16),
                new BlockPos(15, 0, 16),
                new BlockPos(14, 0, 17),
                new BlockPos(15, 0, 17),
                new BlockPos(14, 0, 18),
                new BlockPos(15, 0, 18),
                new BlockPos(16, 0, 16),
                new BlockPos(17, 0, 16),
                new BlockPos(18, 0, 16),
                new BlockPos(16, 0, 17),
                new BlockPos(17, 0, 17),
                new BlockPos(18, 0, 17),
                new BlockPos(16, 0, 18),
                new BlockPos(17, 0, 18),
                new BlockPos(18, 0, 18)
            );
            assertIterableEquals(expected, list);
        }

        @Test
        @DisplayName("crossXZChunk_setCurrent")
        void crossXZChunkSetCurrent() {
            // (14, 14), (19, 19), end exclusive
            // Parameter: (13, 0, 13) -> (19, 0, 19)
            Area area = new Area(13, 0, 13, 19, 0, 19, Direction.NORTH);
            Iterable<BlockPos> iterable = () -> {
                var t = new AdvQuarryTarget.ChunkByChunk(area);
                t.setLastReturned(new BlockPos(15, 0, 18));
                return t;
            };
            var expected = List.of(
                // Current is not included
                // new BlockPos(15, 0, 18),
                new BlockPos(16, 0, 16),
                new BlockPos(17, 0, 16),
                new BlockPos(18, 0, 16),
                new BlockPos(16, 0, 17),
                new BlockPos(17, 0, 17),
                new BlockPos(18, 0, 17),
                new BlockPos(16, 0, 18),
                new BlockPos(17, 0, 18),
                new BlockPos(18, 0, 18)
            );
            var list = asList(iterable);
            assertIterableEquals(expected, list);
        }

        @Test
        void crossXChunk() {
            // (14, 10), (19, 13), end exclusive
            // Parameter: (13, 0, 9) -> (19, 0, 13)
            Area area = new Area(13, 0, 9, 19, 0, 13, Direction.NORTH);
            var list = asList(() -> new AdvQuarryTarget.ChunkByChunk(area));
            var expected = List.of(
                new BlockPos(14, 0, 10),
                new BlockPos(15, 0, 10),
                new BlockPos(14, 0, 11),
                new BlockPos(15, 0, 11),
                new BlockPos(14, 0, 12),
                new BlockPos(15, 0, 12),
                new BlockPos(16, 0, 10),
                new BlockPos(17, 0, 10),
                new BlockPos(18, 0, 10),
                new BlockPos(16, 0, 11),
                new BlockPos(17, 0, 11),
                new BlockPos(18, 0, 11),
                new BlockPos(16, 0, 12),
                new BlockPos(17, 0, 12),
                new BlockPos(18, 0, 12)
            );
            assertIterableEquals(expected, list);
        }

        @Test
        void crossZChunk() {
            // (-12, -34), (-9, -30), end exclusive
            // Parameter: (-13, 0, -35) -> (-9, 0, -30)
            Area area = new Area(-13, 0, -35, -9, 0, -30, Direction.NORTH);
            var list = asList(() -> new AdvQuarryTarget.ChunkByChunk(area));
            var expected = List.of(
                new BlockPos(-12, 0, -34),
                new BlockPos(-11, 0, -34),
                new BlockPos(-10, 0, -34),
                new BlockPos(-12, 0, -33),
                new BlockPos(-11, 0, -33),
                new BlockPos(-10, 0, -33),
                new BlockPos(-12, 0, -32),
                new BlockPos(-11, 0, -32),
                new BlockPos(-10, 0, -32),
                new BlockPos(-12, 0, -31),
                new BlockPos(-11, 0, -31),
                new BlockPos(-10, 0, -31)
            );
            assertIterableEquals(expected, list);
        }

        @Test
        @DisplayName("(0, 0) -> (16, 16), end exclusive")
        void oneChunk1() {
            // (0, 0) -> (16, 16), end exclusive
            Area area = new Area(-1, 0, -1, 16, 0, 16, Direction.NORTH);
            var list = asList(() -> new AdvQuarryTarget.ChunkByChunk(area));
            assertAll(
                () -> assertEquals(new BlockPos(0, 0, 0), list.getFirst()),
                () -> assertFalse(list.contains(new BlockPos(16, 0, 16))),
                () -> assertEquals(256, list.size())
            );
        }

        @Test
        @DisplayName("(0, 0) -> (15, 15), end exclusive")
        void oneChunk2() {
            // (0, 0) -> (15, 15), end exclusive
            Area area = new Area(-1, 0, -1, 15, 0, 15, Direction.NORTH);
            var list = asList(() -> new AdvQuarryTarget.ChunkByChunk(area));
            assertAll(
                () -> assertEquals(new BlockPos(0, 0, 0), list.getFirst()),
                () -> assertFalse(list.contains(new BlockPos(15, 0, 15))),
                () -> assertEquals(225, list.size())
            );
        }

        @Test
        @DisplayName("(0, 0) -> (17, 17), end exclusive")
        void oneChunk3() {
            // (0, 0) -> (17, 17), end exclusive
            Area area = new Area(-1, 0, -1, 17, 0, 17, Direction.NORTH);
            var list = asList(() -> new AdvQuarryTarget.ChunkByChunk(area));
            assertAll(
                () -> assertEquals(new BlockPos(0, 0, 0), list.getFirst()),
                () -> assertTrue(list.contains(new BlockPos(16, 0, 16))),
                () -> assertFalse(list.contains(new BlockPos(17, 0, 17))),
                () -> assertEquals(289, list.size())
            );
        }
    }
}
