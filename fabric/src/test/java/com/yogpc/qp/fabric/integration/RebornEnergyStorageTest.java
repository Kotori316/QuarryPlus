package com.yogpc.qp.fabric.integration;

import com.yogpc.qp.BeforeMC;
import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.PlatformAccessDelegate;
import com.yogpc.qp.config.TestQuarryConfig;
import com.yogpc.qp.fabric.PlatformAccessFabric;
import com.yogpc.qp.fabric.machine.quarry.QuarryEntityFabric;
import com.yogpc.qp.machine.PowerEntity;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RebornEnergyStorageTest extends BeforeMC {
    static QuarryEntityFabric createEntity() {
        var entity = new QuarryEntityFabric(BlockPos.ZERO, PlatformAccess.getAccess().registerObjects().quarryBlock().get().defaultBlockState());
        entity.setTimeProvider(() -> 1L);
        return entity;
    }

    static RebornEnergyStorage createInstance() {
        var entity = createEntity();
        return new RebornEnergyStorage(entity, TestQuarryConfig.defaults());
    }

    @BeforeEach
    void setUp() {
        PlatformAccessDelegate delegate = (PlatformAccessDelegate) PlatformAccess.getAccess();
        delegate.setAccess(new PlatformAccessFabric());
    }

    @Test
    void instance() {
        assertDoesNotThrow(RebornEnergyStorageTest::createInstance);
    }

    @Test
    void getEnergy() {
        var entity = createEntity();
        var instance = new RebornEnergyStorage(entity, TestQuarryConfig.defaults());
        entity.addEnergy(PowerEntity.ONE_FE * 16, false);

        assertEquals(256, instance.getAmount());
    }

    @Test
    void getEnergy2() {
        var entity = createEntity();
        var config = TestQuarryConfig.defaults().withRebornEnergyConversionCoefficient(1d / 4d);

        var instance = new RebornEnergyStorage(entity, config);
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
        var config = TestQuarryConfig.defaults().withRebornEnergyConversionCoefficient(1d / 4d);
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
