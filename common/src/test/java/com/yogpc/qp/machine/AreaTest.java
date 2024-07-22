package com.yogpc.qp.machine;

import com.google.common.collect.Lists;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.NbtOps;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.Duration;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class AreaTest {
    @Test
    void createInstance() {
        assertDoesNotThrow(() -> new Area(0, 0, 0, 3, 4, 5, Direction.NORTH));
    }

    @Nested
    class IsInTest {
        Area area;

        @BeforeEach
        void setup() {
            area = new Area(0, 0, 0, 4, 5, 6, Direction.NORTH);
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 2, 3})
        void inX(int x) {
            assertTrue(area.inAreaX(x), "X(%d) must be in the area".formatted(x));
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, 0, 4, 5,})
        void notInX(int x) {
            assertFalse(area.inAreaX(x), "X(%d) must not be in the area".formatted(x));
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 2, 3, 4, 5})
        void inZ(int z) {
            assertTrue(area.inAreaZ(z), "Z(%d) must be in the area".formatted(z));
        }

        @ParameterizedTest
        @ValueSource(ints = {-1, 0, 6, 7})
        void notInZ(int z) {
            assertFalse(area.inAreaZ(z), "Z(%d) must not be in the area".formatted(z));
        }

        @TestFactory
        Stream<DynamicTest> allIn() {
            var poses = BlockPos.betweenClosedStream(1, 0, 1, 3, 0, 5);
            return poses.map(p -> DynamicTest.dynamicTest(p.toShortString(), () -> assertTrue(area.inAreaXZ(p), "Pos(%s) must be in the area".formatted(p.toShortString()))));
        }

        @ParameterizedTest
        @MethodSource
        void edge(BlockPos pos) {
            assertTrue(area.isEdge(pos));
        }

        static Stream<BlockPos> edge() {
            return Stream.of(
                Stream.of(
                    new BlockPos(0, 0, 0),
                    new BlockPos(0, 0, 6),
                    new BlockPos(4, 0, 6),
                    new BlockPos(4, 0, 0)
                ),
                IntStream.rangeClosed(1, 3).mapToObj(x -> new BlockPos(x, 0, 0)),
                IntStream.rangeClosed(1, 3).mapToObj(x -> new BlockPos(x, 0, 6)),
                IntStream.rangeClosed(1, 5).mapToObj(z -> new BlockPos(0, 1, z)),
                IntStream.rangeClosed(1, 5).mapToObj(z -> new BlockPos(4, 2, z))
            ).flatMap(Function.identity());
        }

        @TestFactory
        Stream<DynamicTest> allNotEdge() {
            var poses = BlockPos.betweenClosedStream(1, 0, 1, 3, 0, 5);
            var others = Stream.of(
                new BlockPos(0, 1, 7),
                new BlockPos(-1, 2, 6),
                new BlockPos(4, 3, 7),
                new BlockPos(5, 4, 6),
                new BlockPos(5, 5, 0),
                new BlockPos(4, 6, -1),
                new BlockPos(5, 7, -1)
            );
            return Stream.concat(poses, others)
                .map(p -> DynamicTest.dynamicTest(p.toShortString(), () -> assertFalse(area.isEdge(p), "Pos(%s) must not be an edge of the area".formatted(p.toShortString()))));
        }

        @Test
        void getEdge1() {
            var edges = area.getEdgeForPos(new BlockPos(1, 0, 1));
            assertEquals(
                Set.of(new BlockPos(0, 0, 1), new BlockPos(1, 0, 0), new BlockPos(0, 0, 0), new BlockPos(2, 0, 0), new BlockPos(0, 0, 2)),
                edges
            );
        }

        @TestFactory
        Stream<DynamicTest> noAdjacentEdges() {
            var poses = IntStream.rangeClosed(2, 4).mapToObj(z -> new BlockPos(2, 0, z));
            return poses.map(p -> DynamicTest.dynamicTest(p.toShortString(), () -> assertEquals(Set.of(), area.getEdgeForPos(p))));
        }
    }

    @Nested
    class ConstructorTest {
        @Test
        void fromVec1() {
            var area = new Area(BlockPos.ZERO, new BlockPos(3, 4, 5), Direction.NORTH);
            var expected = new Area(0, 0, 0, 3, 4, 5, Direction.NORTH);
            assertEquals(expected, area);
        }

        @Test
        void fromVec2() {
            var area = new Area(new BlockPos(3, 4, 5), BlockPos.ZERO, Direction.UP);
            var expected = new Area(0, 0, 0, 3, 4, 5, Direction.UP);
            assertEquals(expected, area);
        }

        @Test
        void invalidParameter1() {
            assertThrows(IllegalArgumentException.class, () -> new Area(5, 1, 2, 0, 3, 4, Direction.UP));
        }
    }

    @ParameterizedTest
    @MethodSource
    <T> void cycle(Area area, DynamicOps<T> ops) {
        var encoded = assertDoesNotThrow(() -> Area.CODEC.codec().encodeStart(ops, area).getOrThrow());
        var decoded = assertDoesNotThrow(() -> Area.CODEC.codec().parse(ops, encoded).getOrThrow());
        assertEquals(area, decoded);
    }

    static Stream<Arguments> cycle() {
        var size = 6;
        var random = new Random();
        return Stream.of(NbtOps.INSTANCE, JsonOps.INSTANCE, JsonOps.COMPRESSED).flatMap(o ->
            random.ints(size).boxed().flatMap(x ->
                random.ints(size).boxed().flatMap(y ->
                    random.ints(size).mapToObj(z -> new Area(x, y, z, x + random.nextInt(300), y + random.nextInt(300), z + random.nextInt(300), Direction.values()[random.nextInt(6)]))
                        .map(area ->
                            Arguments.of(area, o)
                        )
                )
            ));
    }

    @Test
    void parseNull() {
        var parsed = assertDoesNotThrow(() -> Area.CODEC.codec().parse(NbtOps.INSTANCE, null));
        assertTrue(parsed.isError());
    }

    @Nested
    class GetChainTest {
        Area area;

        @BeforeEach
        void setup() {
            area = new Area(0, 0, 0, 4, 5, 6, Direction.NORTH);
        }

        @ParameterizedTest
        @MethodSource("start")
        void allTrue(BlockPos start) {
            var chained = assertDoesNotThrow(() -> assertTimeoutPreemptively(Duration.ofSeconds(1), () -> area.getChainBlocks(start, p -> true, area.maxY())));
            assertFalse(chained.isEmpty());
        }

        @ParameterizedTest
        @MethodSource("start")
        void allFalse(BlockPos start) {
            var chained = assertDoesNotThrow(() -> assertTimeoutPreemptively(Duration.ofSeconds(1), () -> area.getChainBlocks(start, p -> false, area.maxY())));
            assertTrue(chained.isEmpty());
        }

        static Stream<BlockPos> start() {
            return BlockPos.betweenClosedStream(0, 0, 0, 4, 0, 6);
        }

        @Test
        void x3() {
            var chained = assertDoesNotThrow(() -> assertTimeout(Duration.ofSeconds(1), () ->
                area.getChainBlocks(new BlockPos(3, 5, 1), p -> p.getX() == 3, area.maxY())
            ));
            var expected = BlockPos.betweenClosedStream(3, 5, 1, 3, 5, 5)
                .map(BlockPos::immutable)
                .collect(Collectors.toSet());
            assertEquals(expected, chained);
        }
    }

    @Nested
    class QuarryFramePosIteratorTest {
        @Test
        void instance() {
            assertDoesNotThrow(() -> new Area.QuarryFramePosIterator(0, 0, 0, 4, 5, 6));
            assertDoesNotThrow(() -> (new Area(0, 0, 0, 4, 5, 6, Direction.NORTH)).quarryFramePosIterator());
        }

        @Test
        void finite() {
            var itr = new Area.QuarryFramePosIterator(0, 0, 0, 4, 5, 6);
            var list = assertTimeoutPreemptively(Duration.ofSeconds(5), () -> assertDoesNotThrow(() -> Lists.newArrayList(itr)));
            assertNotEquals(0, list.size());
            assertNotEquals(1, list.size());
            System.out.println(list);
        }

        @ParameterizedTest
        @MethodSource
        void setCurrent(BlockPos current, BlockPos next) {
            var itr = new Area.QuarryFramePosIterator(0, 0, 0, 4, 5, 6);
            itr.setLastReturned(current);
            assertEquals(next, assertDoesNotThrow(itr::next));
        }

        static Stream<Arguments> setCurrent() {
            return Stream.of(
                Arguments.of(new BlockPos(3, 5, 0), new BlockPos(4, 5, 0)),
                Arguments.of(new BlockPos(0, 0, 6), new BlockPos(0, 0, 5)),
                Arguments.of(new BlockPos(4, 2, 6), new BlockPos(0, 2, 6))
            );
        }

        @Test
        void setLast() {
            var itr = new Area.QuarryFramePosIterator(0, 0, 0, 4, 5, 6);
            itr.setLastReturned(new BlockPos(0, 5, 1));
            assertFalse(itr.hasNext());
            assertThrows(NoSuchElementException.class, itr::next);
        }
    }

    @Nested
    class QuarryDigPosIteratorTest {
        Area area;

        @BeforeEach
        void setUp() {
            area = new Area(0, 0, 0, 4, 5, 6, Direction.NORTH);
        }

        @Test
        void instance() {
            assertDoesNotThrow(() -> new Area.QuarryDigPosIterator(area, 10));
            assertDoesNotThrow(() -> area.quarryDigPosIterator(10));
        }

        @ParameterizedTest
        @ValueSource(ints = {10, 11, 0, -1})
        void finite(int y) {
            var itr = area.quarryDigPosIterator(y);
            var list = assertTimeoutPreemptively(Duration.ofSeconds(5), () -> assertDoesNotThrow(() -> Lists.newArrayList(itr)));
            assertEquals(15, list.size());
            System.out.println(list);
        }

        @Test
        void sizeCalculate() {
            var size = (area.maxX() - area.minX() - 1) * (area.maxZ() - area.minZ() - 1);
            assertEquals(15, size);
        }

        @Test
        void sameEdge() {
            var list1 = assertTimeoutPreemptively(Duration.ofSeconds(5), () -> assertDoesNotThrow(() -> Lists.newArrayList(area.quarryDigPosIterator(0))));
            var list2 = assertTimeoutPreemptively(Duration.ofSeconds(5), () -> assertDoesNotThrow(() -> Lists.newArrayList(area.quarryDigPosIterator(1))));

            // First
            assertEquals(list1.getFirst().getX(), list2.getLast().getX());
            assertEquals(list1.getFirst().getZ(), list2.getLast().getZ());
            assertNotEquals(list1.getFirst().getY(), list2.getLast().getY());

            // Last
            assertEquals(list1.getLast().getX(), list2.getFirst().getX());
            assertEquals(list1.getLast().getZ(), list2.getFirst().getZ());
            assertNotEquals(list1.getLast().getY(), list2.getLast().getY());
        }

        @Test
        void setCurrent1() {
            var itr = area.quarryDigPosIterator(1);
            assertNull(itr.getLastReturned());
            var pos = new BlockPos(1, 1, 3);
            itr.setLastReturned(pos);
            assertEquals(pos, itr.getLastReturned());
            assertTrue(itr.hasNext());
            assertEquals(new BlockPos(1, 1, 2), itr.next());
            assertEquals(new BlockPos(1, 1, 2), itr.getLastReturned());
            assertEquals(new BlockPos(1, 1, 1), itr.next());
        }

        @Test
        void setCurrent2() {
            var itr = area.quarryDigPosIterator(2);
            assertNull(itr.getLastReturned());
            var pos = new BlockPos(1, 2, 3);
            itr.setLastReturned(pos);
            assertEquals(pos, itr.getLastReturned());
            assertTrue(itr.hasNext());
            assertEquals(new BlockPos(1, 2, 4), itr.next());
            assertEquals(new BlockPos(1, 2, 4), itr.getLastReturned());
            assertEquals(new BlockPos(1, 2, 5), itr.next());
        }
    }
}
