package com.yogpc.qp.machines.quarry;

import com.yogpc.qp.QuarryPlusTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TileQuarryTest extends QuarryPlusTest {

    @Test
    void headSpeed() {
        for (int i = 0; i < 5; i++) {
            var i0 = TileQuarry.headSpeed(i);
            var i1 = TileQuarry.headSpeed(i + 1);
            assertTrue(i0 < i1, String.format("%f should be grater than %f", i1, i0));
        }
    }

    @Test
    @DisplayName("Head should be able to move more than 1 block/tick")
    void headSpeedAtE5() {
        var i5 = TileQuarry.headSpeed(5);
        assertTrue(i5 >= 1);
    }
}
