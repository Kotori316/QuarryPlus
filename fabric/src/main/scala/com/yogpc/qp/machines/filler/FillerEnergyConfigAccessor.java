package com.yogpc.qp.machines.filler;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.EnergyConfigAccessor;

enum FillerEnergyConfigAccessor implements EnergyConfigAccessor {
    INSTANCE;

    @Override
    public double makeFrame() {
        return EnergyConfigAccessor.ONES.makeFrame();
    }

    @Override
    public double moveHead() {
        return EnergyConfigAccessor.ONES.moveHead();
    }

    @Override
    public double breakBlock() {
        return QuarryPlus.config.filler.fillerEnergyBreakBlock;
    }

    @Override
    public double removeFluid() {
        return EnergyConfigAccessor.ONES.removeFluid();
    }
}
