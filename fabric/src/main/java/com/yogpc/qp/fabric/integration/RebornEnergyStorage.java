package com.yogpc.qp.fabric.integration;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.config.QuarryConfig;
import com.yogpc.qp.machine.PowerEntity;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;
import team.reborn.energy.api.EnergyStorage;

final class RebornEnergyStorage extends SnapshotParticipant<Long> implements EnergyStorage {
    public final long conversionRate;
    private final PowerEntity powerEntity;

    @Nullable
    static EnergyStorage provider(Object blockEntity, Direction ignored) {
        if (blockEntity instanceof PowerEntity entity) {
            return new RebornEnergyStorage(entity, PlatformAccess.config());
        }
        return null;
    }

    RebornEnergyStorage(PowerEntity powerEntity, QuarryConfig config) {
        this.powerEntity = powerEntity;
        this.conversionRate = (long) (PowerEntity.ONE_FE * config.rebornEnergyConversionCoefficient());
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
        var accepted = powerEntity.addEnergy(maxAmount * conversionRate, false);
        return accepted / conversionRate;
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
        return powerEntity.getEnergy() / conversionRate;
    }

    @Override
    public long getCapacity() {
        return powerEntity.getMaxEnergy() / conversionRate;
    }
}
