package com.yogpc.qp.integration;

import java.util.EnumSet;

import com.yogpc.qp.machines.PowerTile;
import team.reborn.energy.EnergySide;
import team.reborn.energy.EnergyStorage;
import team.reborn.energy.EnergyTier;

public class RebornEnergyStorage implements EnergyStorage {
    public static final long CONVERSION_RATE = PowerTile.ONE_FE / 16;
    private final PowerTile powerTile;
    private final EnumSet<EnergySide> acceptableSize = EnumSet.allOf(EnergySide.class);

    public RebornEnergyStorage(PowerTile powerTile) {
        this.powerTile = powerTile;
    }

    @Override
    public double getStored(EnergySide face) {
        if (acceptableSize.contains(face))
            return ((double) powerTile.getEnergy()) / CONVERSION_RATE;
        else
            return 0d;
    }

    @Override
    public void setStored(double amount) {
        long current = powerTile.getEnergy();
        long updated = (long) amount * CONVERSION_RATE;
        powerTile.addEnergy(updated - current);
    }

    @Override
    public double getMaxStoredPower() {
        return ((double) powerTile.getMaxEnergy()) / CONVERSION_RATE;
    }

    @Override
    public EnergyTier getTier() {
        return EnergyTier.INSANE;
    }

    @Override
    public double getMaxInput(EnergySide side) {
        if (acceptableSize.contains(side)) return EnergyStorage.super.getMaxInput(side);
        else return 0d;
    }

    @Override
    public double getMaxOutput(EnergySide side) {
        return 0; // This is not generator or storage. This machine only consumes energy.
    }
}
