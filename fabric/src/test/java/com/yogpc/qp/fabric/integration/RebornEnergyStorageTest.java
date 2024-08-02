package com.yogpc.qp.fabric.integration;

import com.yogpc.qp.BeforeMC;
import com.yogpc.qp.config.QuarryConfig;
import com.yogpc.qp.fabric.machine.quarry.QuarryEntityFabric;
import com.yogpc.qp.machine.PowerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RebornEnergyStorageTest extends BeforeMC {
    static RebornEnergyStorage createInstance() {
        return new RebornEnergyStorage(new QuarryEntityFabric(BlockPos.ZERO, Blocks.AIR.defaultBlockState()), QuarryConfig.defaultConfig(false));
    }

    @Test
    void instance() {
        assertDoesNotThrow(RebornEnergyStorageTest::createInstance);
    }

    @Test
    void getEnergy() {
        var entity = new QuarryEntityFabric(BlockPos.ZERO, Blocks.AIR.defaultBlockState());
        entity.setTimeProvider(() -> 1L);
        var instance = new RebornEnergyStorage(entity, QuarryConfig.defaultConfig(false));
        entity.addEnergy(PowerEntity.ONE_FE * 16, false);

        assertEquals(256, instance.getAmount());
    }
}
