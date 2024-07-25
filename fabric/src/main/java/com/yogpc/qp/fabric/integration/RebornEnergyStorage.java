package com.yogpc.qp.fabric.integration;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.machine.PowerEntity;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import team.reborn.energy.api.EnergyStorage;

final class RebornEnergyStorage extends SnapshotParticipant<Long> implements EnergyStorage {
    public static final long CONVERSION_RATE = (long) (PowerEntity.ONE_FE * PlatformAccess.getConfig().rebornEnergyConversionCoefficient());
    private final PowerEntity powerEntity;

    RebornEnergyStorage(PowerEntity powerEntity) {
        this.powerEntity = powerEntity;
    }

    @Override
    protected Long createSnapshot() {
        return powerEntity.getEnergy();
    }

    @Override
    protected void readSnapshot(Long snapshot) {
        powerEntity.setEnergy(snapshot, false);
    }

    @Override
    public long insert(long maxAmount, TransactionContext transaction) {
        long insertSimulate = Math.min(maxAmount, getCapacity() - getAmount());
        if (insertSimulate <= 0) return 0;

        updateSnapshots(transaction);
        var accepted = powerEntity.addEnergy(maxAmount * CONVERSION_RATE, false);
        return accepted / CONVERSION_RATE;
    }

    @Override
    public long extract(long maxAmount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public boolean supportsExtraction() {
        return false;
    }

    @Override
    public long getAmount() {
        return powerEntity.getEnergy() / CONVERSION_RATE;
    }

    @Override
    public long getCapacity() {
        return powerEntity.getMaxEnergy() / CONVERSION_RATE;
    }
}
