package com.yogpc.qp.integration;
/*
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.PowerTile;
import com.yogpc.qp.machines.misc.CreativeGeneratorTile;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import team.reborn.energy.api.EnergyStorage;

@SuppressWarnings("UnstableApiUsage")
final class RebornEnergyGenerator extends SnapshotParticipant<Long> implements EnergyStorage {
    public static final long CONVERSION_RATE = (long) (PowerTile.ONE_FE * QuarryPlus.config.power.rebornEnergyConversionCoefficient);

    private final CreativeGeneratorTile generator;

    RebornEnergyGenerator(CreativeGeneratorTile generator) {
        this.generator = generator;
    }

    @Override
    protected Long createSnapshot() {
        return generator.getSendEnergy();
    }

    @Override
    protected void readSnapshot(Long snapshot) {
        generator.setSendEnergy(snapshot);
    }

    @Override
    public long insert(long maxAmount, TransactionContext transaction) {
        return 0; // not insertable.
    }

    @Override
    public boolean supportsInsertion() {
        return false;
    }

    @Override
    public long extract(long maxAmount, TransactionContext transaction) {
        return Math.min(maxAmount, generator.getSendEnergy() / CONVERSION_RATE);
    }

    @Override
    public long getAmount() {
        return generator.getEnergy() / CONVERSION_RATE;
    }

    @Override
    public long getCapacity() {
        return generator.getMaxEnergy() / CONVERSION_RATE;
    }
}
*/