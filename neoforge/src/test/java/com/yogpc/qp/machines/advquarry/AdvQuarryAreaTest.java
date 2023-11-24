package com.yogpc.qp.machines.advquarry;

import java.util.stream.IntStream;

import com.yogpc.qp.QuarryPlusTest;
import com.yogpc.qp.machines.Area;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ChunkPos;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(QuarryPlusTest.class)
final class AdvQuarryAreaTest {
    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 16, 17})
    void noLimit(int limit) {
        var pos = new BlockPos(34, 5, 2);
        var area = BlockAdvQuarry.createDefaultArea(pos, Direction.NORTH, limit);
        var expected = new Area(31, 5, -1, 48, 9, 16, Direction.NORTH);
        assertEquals(expected, area);
        assertTrue(area.isRangeInLimit(limit, true));
    }

    @Test
    void limit4() {
        int limit = 4;
        var pos = new BlockPos(34, 5, 2);
        var area = BlockAdvQuarry.createDefaultArea(pos, Direction.NORTH, limit);
        var expected = new Area(31, 5, -1, 36, 9, 4, Direction.NORTH);
        assertEquals(expected, area);
        assertTrue(area.isRangeInLimit(limit, true));
    }

    @Test
    void limit6_1() {
        int limit = 6;
        var pos = new BlockPos(34, 5, 2);
        var area = BlockAdvQuarry.createDefaultArea(pos, Direction.NORTH, limit);
        var expected = new Area(31, 5, -1, 38, 9, 6, Direction.NORTH);
        assertEquals(expected, area);
        assertTrue(area.isRangeInLimit(limit, true));
    }

    @Test
    void limit6_2() {
        int limit = 6;
        var pos = new BlockPos(42, 5, 9);
        var area = BlockAdvQuarry.createDefaultArea(pos, Direction.NORTH, limit);
        var expected = new Area(38, 5, 5, 45, 9, 12, Direction.NORTH);
        assertEquals(expected, area);
        assertTrue(area.isRangeInLimit(limit, true));
    }

    @Test
    void limit6_3() {
        int limit = 6;
        var pos = new BlockPos(12, 5, 30);
        var area = BlockAdvQuarry.createDefaultArea(pos, Direction.NORTH, limit);
        var expected = new Area(8, 5, 25, 15, 9, 32, Direction.NORTH);
        assertEquals(expected, area);
        assertTrue(area.isRangeInLimit(limit, true));
    }

    @Test
    void limit10() {
        int limit = 10;
        var pos = new BlockPos(12, 5, 30);
        var area = BlockAdvQuarry.createDefaultArea(pos, Direction.NORTH, limit);
        var expected = new Area(5, 5, 21, 16, 9, 32, Direction.NORTH);
        assertEquals(expected, area);
        assertTrue(area.isRangeInLimit(limit, true));
    }

    @Test
    void limit9() {
        int limit = 9;
        var pos = new BlockPos(42, 5, 9);
        var area = BlockAdvQuarry.createDefaultArea(pos, Direction.NORTH, limit);
        var expected = new Area(37, 5, 4, 47, 9, 14, Direction.NORTH);
        assertEquals(expected, area);
        assertTrue(area.isRangeInLimit(limit, true));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17})
    void areaIsInOneChunk(int limit) {
        var chunkPos = new ChunkPos(new BlockPos(32, 0, 0));
        assertAll(IntStream.range(32, 48).boxed().flatMap(x ->
                IntStream.range(0, 16).mapToObj(z -> new BlockPos(x, 5, z)))
            .map(p -> BlockAdvQuarry.createDefaultArea(p, Direction.NORTH, limit))
            .map(a -> a.shrink(1, 0, 1))
            .flatMap(a -> BlockPos.betweenClosedStream(a.minX(), a.minY(), a.minZ(), a.maxX(), a.minY(), a.maxZ()))
            .map(ChunkPos::new)
            .map(p -> () -> assertEquals(chunkPos, p)));
    }
}
