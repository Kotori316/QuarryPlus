package com.yogpc.qp.fabric.integration;

import com.yogpc.qp.BeforeMC;
import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.PlatformAccessDelegate;
import com.yogpc.qp.config.QuarryConfig;
import com.yogpc.qp.fabric.PlatformAccessFabric;
import com.yogpc.qp.fabric.machine.quarry.QuarryEntityFabric;
import com.yogpc.qp.machine.PowerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RebornEnergyStorageTest extends BeforeMC {
    static RebornEnergyStorage createInstance() {
        return new RebornEnergyStorage(new QuarryEntityFabric(BlockPos.ZERO, Blocks.AIR.defaultBlockState()), QuarryConfig.defaultConfig(false));
    }

    @TempDir
    Path configDir;

    @BeforeEach
    void setUp() {
        PlatformAccessDelegate delegate = (PlatformAccessDelegate) PlatformAccess.getAccess();
        delegate.setAccess(new PlatformAccessFabric());
        delegate.setConfigPath(configDir.resolve("RebornEnergyStorageTest.toml"));
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
