package com.yogpc.qp.fabric.integration;

import com.electronwill.nightconfig.core.Config;
import com.yogpc.qp.BeforeMC;
import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.PlatformAccessDelegate;
import com.yogpc.qp.config.QuarryConfig;
import com.yogpc.qp.fabric.PlatformAccessFabric;
import com.yogpc.qp.fabric.machine.quarry.QuarryEntityFabric;
import com.yogpc.qp.machine.PowerEntity;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class RebornEnergyStorageTest extends BeforeMC {
    static QuarryEntityFabric createEntity() {
        var entity = new QuarryEntityFabric(BlockPos.ZERO, PlatformAccess.getAccess().registerObjects().quarryBlock().get().defaultBlockState());
        entity.setTimeProvider(() -> 1L);
        return entity;
    }

    static RebornEnergyStorage createInstance() {
        var entity = createEntity();
        return new RebornEnergyStorage(entity, QuarryConfig.defaultConfig(false));
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
        var entity = createEntity();
        var instance = new RebornEnergyStorage(entity, QuarryConfig.defaultConfig(false));
        entity.addEnergy(PowerEntity.ONE_FE * 16, false);

        assertEquals(256, instance.getAmount());
    }

    @Test
    void getEnergy2() {
        var entity = createEntity();
        var config = Config.inMemory();
        config.set("rebornEnergyConversionCoefficient", 1d / 4d);

        var instance = new RebornEnergyStorage(entity, QuarryConfig.fromConfig(config, false));
        entity.addEnergy(PowerEntity.ONE_FE * 16, false);

        assertEquals(64, instance.getAmount());
    }

    @Test
    void getMaxEnergy() {
        var instance = createInstance();

        // 1E = 1/16 FE, 10000 * 16E = 10000FE
        assertEquals(10000 * 16, instance.getCapacity());
    }

    @Test
    void getMaxEnergy2() {
        var entity = createEntity();
        var c = Config.inMemory();
        c.set("rebornEnergyConversionCoefficient", 1d / 4d);
        var config = QuarryConfig.fromConfig(c, false);
        var instance = new RebornEnergyStorage(entity, config);

        assertEquals(config.powerMap().quarry().maxEnergy() * 4, instance.getCapacity());
    }

    @Test
    void noExtraction() {
        var instance = createInstance();
        assertFalse(instance.supportsExtraction());
        try (var tx = Transaction.openOuter()) {
            assertEquals(0, instance.extract(Long.MAX_VALUE, tx));
        }
    }
}
