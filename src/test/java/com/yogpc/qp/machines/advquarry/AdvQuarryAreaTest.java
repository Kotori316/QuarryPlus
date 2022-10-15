package com.yogpc.qp.machines.advquarry;

import com.yogpc.qp.QuarryPlusTest;
import com.yogpc.qp.machines.Area;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(QuarryPlusTest.class)
final class AdvQuarryAreaTest {
    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 16, 17})
    void noLimit(int limit) {
        var pos = new BlockPos(34, 5, 2);
        var area = BlockAdvQuarry.createDefaultArea(pos, Direction.NORTH, limit);
        var expected = new Area(31, 5, -1, 48, 9, 16, Direction.NORTH);
        assertEquals(expected, area);
    }

    @Test
    void limit4() {
        int limit = 4;
        var pos = new BlockPos(34, 5, 2);
        var area = BlockAdvQuarry.createDefaultArea(pos, Direction.NORTH, limit);
        var expected = new Area(31, 5, -1, 37, 9, 5, Direction.NORTH);
        assertEquals(expected, area);
    }

    @Test
    void limit6_1() {
        int limit = 6;
        var pos = new BlockPos(34, 5, 2);
        var area = BlockAdvQuarry.createDefaultArea(pos, Direction.NORTH, limit);
        var expected = new Area(31, 5, -1, 39, 9, 7, Direction.NORTH);
        assertEquals(expected, area);
    }

    @Test
    void limit6_2() {
        int limit = 6;
        var pos = new BlockPos(42, 5, 9);
        var area = BlockAdvQuarry.createDefaultArea(pos, Direction.NORTH, limit);
        var expected = new Area(38, 5, 5, 46, 9, 13, Direction.NORTH);
        assertEquals(expected, area);
    }

    @Test
    void limit9() {
        int limit = 9;
        var pos = new BlockPos(42, 5, 9);
        var area = BlockAdvQuarry.createDefaultArea(pos, Direction.NORTH, limit);
        var expected = new Area(37, 5, 4, 48, 9, 15, Direction.NORTH);
        assertEquals(expected, area);
    }
}
