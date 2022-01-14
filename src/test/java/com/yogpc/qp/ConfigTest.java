package com.yogpc.qp;

import com.yogpc.qp.machines.PowerTile;
import com.yogpc.qp.machines.quarry.QuarryBlock;
import com.yogpc.qp.machines.quarry.TileQuarry;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

final class ConfigTest extends QuarryPlusTest {

    @Test
    @DisplayName("Change maxEnergy of a machine.")
    void changeMaxEnergy() {
        var before = QuarryPlus.config.powerMap.get(QuarryBlock.NAME, "maxEnergy").orElse(-4);
        assertNotEquals(-4, before);
        try {
            QuarryPlus.config.powerMap.map.get(QuarryBlock.NAME).get("maxEnergy").set(60000d);
            var tile = new TileQuarry(BlockPos.ZERO, Holder.BLOCK_QUARRY.defaultBlockState());
            assertEquals(60000 * PowerTile.ONE_FE, tile.getMaxEnergy());
            assertEquals(60000 * PowerTile.ONE_FE, tile.getPowerConfig().maxEnergy());
        } finally {
            QuarryPlus.config.powerMap.map.get(QuarryBlock.NAME).get("maxEnergy").set(before);
        }
    }

    @Test
    @DisplayName("Change FrameEnergy of a machine")
    void changeFrameEnergy() {
        var before = QuarryPlus.config.powerMap.get(QuarryBlock.NAME, "makeFrame").orElse(-4d);
        assertNotEquals(-4d, before);
        try {
            QuarryPlus.config.powerMap.map.get(QuarryBlock.NAME).get("makeFrame").set(40d);
            var tile = new TileQuarry(BlockPos.ZERO, Holder.BLOCK_QUARRY.defaultBlockState());
            assertEquals(40d * PowerTile.ONE_FE, tile.getPowerConfig().makeFrame());
        } finally {
            QuarryPlus.config.powerMap.map.get(QuarryBlock.NAME).get("makeFrame").set(before);
        }
    }
}