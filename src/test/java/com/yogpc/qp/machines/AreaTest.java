package com.yogpc.qp.machines;

import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AreaTest {
    Area area = new Area(3, 4, 5, 9, 8, 7, Direction.UP);

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
    void isInAreaIgnoreY() {
        Set<BlockPos> inner = BlockPos.betweenClosedStream(4, 0, 6, 8, 55, 6).map(BlockPos::immutable).collect(Collectors.toUnmodifiableSet());
        assertAll(inner.stream().map(p -> () -> assertTrue(area.isInAreaIgnoreY(p), "Should be in area: %s".formatted(p))));

        Stream<BlockPos> outer = BlockPos.betweenClosedStream(area.minX(), 0, area.minZ(), area.maxX(), 55, area.maxZ())
            .map(BlockPos::immutable)
            .filter(Predicate.not(inner::contains));
        assertAll(outer.map(p -> () -> assertFalse(area.isInAreaIgnoreY(p), "Should not be in area: %s".formatted(p))));
    }

    @Test
    void deserializeInvalid() {
        assertAll(
            () -> assertTrue(Area.fromNBT(new CompoundTag()).isEmpty()),
            () -> assertTrue(Area.fromNBT(null).isEmpty())
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
        Random random = new Random(564);
        return Stream.concat(Stream.generate(() ->
                new Area(new Vec3i(random.nextInt(), random.nextInt(), random.nextInt()),
                    new Vec3i(random.nextInt(), random.nextInt(), random.nextInt()), Direction.getRandom(random)))
                .limit(50),
            Stream.generate(() ->
                new Area(new Vec3i(random.nextInt(1024) - 512, random.nextInt(1024) - 512, random.nextInt(1024) - 512),
                    new Vec3i(random.nextInt(1024) - 512, random.nextInt(1024) - 512, random.nextInt(1024) - 512), Direction.getRandom(random)))
                .limit(50));
    }

    @Test
    void dummy() {
        assertTrue(randomArea().findAny().isPresent());
    }
}