package com.yogpc.qp.machines.quarry;

import java.util.List;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlusTest;
import com.yogpc.qp.machines.EnchantmentLevel;
import com.yogpc.qp.machines.PowerTile;
import com.yogpc.qp.machines.PowerTileTest;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.enchantment.Enchantments;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(QuarryPlusTest.class)
class TileQuarryTest {

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

    @Test
    void createInstance() {
        assertDoesNotThrow(() -> new TileQuarry(BlockPos.ZERO, Holder.BLOCK_QUARRY.defaultBlockState()));
    }

    @Test
    void energyCapacityTest() {
        PowerTileTest.capacityTest(new TileQuarry(BlockPos.ZERO, Holder.BLOCK_QUARRY.defaultBlockState()));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4, 5, 6, 10})
    void increasedCapacity(int level) {
        var tile = new TileQuarry(BlockPos.ZERO, Holder.BLOCK_QUARRY.defaultBlockState());
        tile.setEnchantments(List.of(new EnchantmentLevel(Enchantments.BLOCK_EFFICIENCY, level)));
        double maxEnergy = tile.getMaxEnergy();
        assertTrue(Math.log10(maxEnergy / PowerTile.ONE_FE) < 6, "Energy: %f > 10^6.");
    }
}
