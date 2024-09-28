package com.yogpc.qp.machine.advquarry;

import com.yogpc.qp.machine.Area;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AdvQuarryBlockTest {
    @Nested
    class CreateDefaultAreaTest {
        @ParameterizedTest
        @EnumSource(Direction.class)
        void testCreateDefaultArea(Direction direction) {
            var area = assertDoesNotThrow(() -> AdvQuarryBlock.createDefaultArea(new BlockPos(3, 4, 5), direction, 16));
            assertEquals(
                new Area(-1, 4, -1, 16, 8, 16, direction),
                area
            );
        }
    }
}
