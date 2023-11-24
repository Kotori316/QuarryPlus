package com.yogpc.qp.integration;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.PowerTile;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import team.reborn.energy.api.EnergyStorage;

@SuppressWarnings({"UnstableApiUsage"})
class RebornEnergyStorage extends SnapshotParticipant<Long> implements EnergyStorage {
    public static final long CONVERSION_RATE = (long) (PowerTile.ONE_FE * QuarryPlus.config.power.rebornEnergyConversionCoefficient);
    private final PowerTile powerTile;

    public RebornEnergyStorage(PowerTile powerTile) {
        this.powerTile = powerTile;
    }

    @Override
    public long getAmount() {
        return powerTile.getEnergy() / CONVERSION_RATE;
    }

    @Override
    protected void readSnapshot(Long snapshot) {
        long current = powerTile.getEnergy();
        long updated = snapshot;
        powerTile.addEnergy(updated - current, false);
    }

    @Override
    public long insert(long maxAmount, TransactionContext transaction) {
        long insertSimulate = Math.min(maxAmount, getCapacity() - getAmount());
        if (insertSimulate > 0) {
            updateSnapshots(transaction);
            long inserted = powerTile.addEnergy(insertSimulate * CONVERSION_RATE, false);
            return inserted / CONVERSION_RATE;
        } else {
            return 0;
        }
    }

    @Override
    public long getCapacity() {
        return powerTile.getMaxEnergy() / CONVERSION_RATE;
    }

    @Override
    public boolean supportsExtraction() {
        return false; // This is not generator or storage. This machine only consumes energy.
    }

    @Override
    public long extract(long maxAmount, TransactionContext transaction) {
        return 0; // Not extractable.
    }

    @Override
    protected Long createSnapshot() {
        return powerTile.getEnergy();
    }
}
