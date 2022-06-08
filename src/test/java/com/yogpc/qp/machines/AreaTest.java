package com.yogpc.qp.machines;

import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AreaTest {
    Area area = new Area(3, 4, 5, 9, 8, 7, Direction.UP);

    @Test
    void badArguments() {
        assertAll(
            () -> assertThrows(IllegalArgumentException.class, () ->
                new Area(0, 0, 0, -1, 0, 0, Direction.UP)),
            () -> assertThrows(IllegalArgumentException.class, () ->
                new Area(0, 0, 0, 0, -1, 0, Direction.UP)),
            () -> assertThrows(IllegalArgumentException.class, () ->
                new Area(0, 0, 0, 0, 0, -1, Direction.UP))
        );
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4})
    void assureY1(int minSpaceY) {
        var assumed = area.assureY(minSpaceY);
        assertEquals(area, assumed);
    }

    @ParameterizedTest
    @ValueSource(ints = {5, 10, 20, 50, 100})
    void assure2(int minSpaceY) {
        var assumed = area.assureY(minSpaceY);
        assertEquals(new Area(3, 4, 5, 9, 4 + minSpaceY, 7, Direction.UP), assumed);
    }

    @Test
    @DisplayName("Above: New Y is 5, updated")
    void aboveY1() {
        var newArea = area.aboveY(5);
        assertEquals(5, newArea.minY());
        assertEquals(new Area(3, 5, 5, 9, 8, 7, Direction.UP), newArea);
    }

    @Test
    @DisplayName("Above: New Y is 4, not updated")
    void aboveY2() {
        var newArea = area.aboveY(4);
        assertEquals(4, newArea.minY());
        assertEquals(area, newArea);
    }

    @Test
    @DisplayName("Above: New Y is 0, not updated")
    void aboveY3() {
        var newArea = area.aboveY(0);
        assertEquals(4, newArea.minY());
        assertEquals(area, newArea);
    }

    @Test
    @DisplayName("Above: New Y is 8, updated")
    void aboveY5() {
        var newArea = area.aboveY(8);
        assertEquals(8, newArea.minY());
        assertEquals(new Area(3, 8, 5, 9, 8, 7, Direction.UP), newArea);
    }

    @ParameterizedTest
    @DisplayName("Above: New Y is over 9, not updated")
    @ValueSource(ints = {10, 20, 50, 100, 9})
    void aboveY6(int newY) {
        var newArea = area.aboveY(newY);
        assertEquals(4, newArea.minY());
        assertEquals(area, newArea);
    }

    @Test
    void isInAreaIgnoreY() {
        Set<BlockPos> inner = BlockPos.betweenClosedStream(4, 0, 6, 8, 55, 6).map(BlockPos::immutable).collect(Collectors.toUnmodifiableSet());
        assertAll(inner.stream().map(p -> () -> assertTrue(area.isInAreaIgnoreY(p), "Should be in area: %s".formatted(p))));

        Stream<BlockPos> outer = BlockPos.betweenClosedStream(area.minX(), 0, area.minZ(), area.maxX(), 55, area.maxZ())
            .map(BlockPos::immutable)
            .filter(Predicate.not(inner::contains));
        assertAll(outer.map(p -> () -> assertFalse(area.isInAreaIgnoreY(p), "Should not be in area: %s".formatted(p))));
    }

    @TestFactory
    Stream<DynamicTest> deserializeInvalid() {
        return Stream.of(
            DynamicTest.dynamicTest("Empty", () -> assertTrue(Area.fromNBT(new CompoundTag()).isEmpty())),
            DynamicTest.dynamicTest("Null", () -> assertTrue(Area.fromNBT(null).isEmpty()))
        );
    }

    @Test
    void serializeFixed() {
        var tag = area.toNBT();
        var deserialized = Area.fromNBT(tag);
        assertEquals(Optional.of(area), deserialized);
    }

    @ParameterizedTest
    @MethodSource("randomArea")
    void serializeRandom(Area area) {
        var tag = area.toNBT();
        var deserialized = Area.fromNBT(tag);
        assertEquals(Optional.of(area), deserialized);
    }

    static Stream<Area> randomArea() {
        var random = RandomSource.create(564);
        return Stream.concat(Stream.generate(() ->
                    new Area(new Vec3i(random.nextInt(), random.nextInt(), random.nextInt()),
                        new Vec3i(random.nextInt(), random.nextInt(), random.nextInt()), Direction.getRandom(random)))
                .limit(50),
            Stream.generate(() ->
                    new Area(new Vec3i(random.nextInt(1024) - 512, random.nextInt(1024) - 512, random.nextInt(1024) - 512),
                        new Vec3i(random.nextInt(1024) - 512, random.nextInt(1024) - 512, random.nextInt(1024) - 512), Direction.getRandom(random)))
                .limit(50));
    }

    @Nested
    class ToTest {
        @Test
        @DisplayName("0 to 4")
        void intStreamTo1() {
            var array = Area.to(0, 4).toArray();
            assertArrayEquals(new int[]{0, 1, 2, 3, 4}, array);
        }

        @Test
        @DisplayName("-2 to 3")
        void intStreamTo2() {
            var array = Area.to(-2, 3).toArray();
            assertArrayEquals(new int[]{-2, -1, 0, 1, 2, 3}, array);
        }

        @Test
        @DisplayName("4 to 0")
        void intStreamTo3() {
            var array = Area.to(4, 0).toArray();
            assertArrayEquals(new int[]{4, 3, 2, 1, 0}, array);
        }

        @Test
        @DisplayName("3 to -2")
        void intStreamTo4() {
            var array = Area.to(3, -2).toArray();
            assertArrayEquals(new int[]{3, 2, 1, 0, -1, -2}, array);
        }

        @Test
        @DisplayName("4 to 4")
        void intStreamTo5() {
            var array = Area.to(4, 4).toArray();
            assertArrayEquals(new int[]{4}, array);
        }
    }

    @Nested
    class ShrinkTest {
        @Test
        void shrinkX1() {
            var shrunk = area.shrink(1, 0, 0);
            assertEquals(new Area(4, 4, 5, 8, 8, 7, Direction.UP), shrunk);
        }

        @Test
        void shrinkX3() {
            var shrunk = area.shrink(3, 0, 0);
            assertEquals(new Area(6, 4, 5, 6, 8, 7, Direction.UP), shrunk);
        }

        @Test
        void shrinkX4() {
            var shrunk = area.shrink(4, 0, 0);
            assertEquals(new Area(5, 4, 5, 7, 8, 7, Direction.UP), shrunk);
        }

        @Test
        void shrinkY1() {
            var shrunk = area.shrink(0, 1, 0);
            assertEquals(new Area(3, 5, 5, 9, 7, 7, Direction.UP), shrunk);
        }

        @Test
        void shrinkY2() {
            var shrunk = area.shrink(0, 2, 0);
            assertEquals(new Area(3, 6, 5, 9, 6, 7, Direction.UP), shrunk);
        }

        @Test
        void shrinkZ1() {
            var shrunk = area.shrink(0, 0, 1);
            assertEquals(new Area(3, 4, 6, 9, 8, 6, Direction.UP), shrunk);
        }
    }

    @Test
    void dummy() {
        assertTrue(randomArea().findAny().isPresent());
    }
}