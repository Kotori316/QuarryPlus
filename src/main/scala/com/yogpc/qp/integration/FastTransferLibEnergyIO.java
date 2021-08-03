package com.yogpc.qp.integration;

import com.yogpc.qp.QuarryConfig;
import com.yogpc.qp.machines.PowerTile;
import dev.technici4n.fasttransferlib.api.Simulation;
import dev.technici4n.fasttransferlib.api.energy.EnergyIo;
import net.minecraft.util.math.Direction;

class FastTransferLibEnergyIO implements EnergyIo {
    public static final long CONVERSION_RATE = (long) (PowerTile.ONE_FE * QuarryConfig.config.fastTransferEnergyConversionCoefficient);
    private final PowerTile powerTile;
    private final boolean accept;

    public FastTransferLibEnergyIO(PowerTile powerTile, Direction context) {
        this.powerTile = powerTile;
        this.accept = context != null;
    }

    @Override
    public double getEnergy() {
        return (double) powerTile.getEnergy() / CONVERSION_RATE;
    }

    @Override
    public boolean supportsExtraction() {
        return false;
    }

    @Override
    public boolean supportsInsertion() {
        return true;
    }

    @Override
    public double getEnergyCapacity() {
        return (double) powerTile.getMaxEnergy() / CONVERSION_RATE;
    }

    @Override
    public double insert(double amount, Simulation simulation) {
        long inserted = powerTile.addEnergy((long) (amount * CONVERSION_RATE), simulation.isSimulating());
        return amount - ((double) inserted / CONVERSION_RATE);
    }

    @Override
    public String toString() {
        return "FastTransferLibEnergyIO{" +
            "tile=" + powerTile +
            "accept=" + accept +
            '}';
    }
}
