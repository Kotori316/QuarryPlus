package com.yogpc.qp.machines.quarry;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.EnergyConfigAccessor;

enum QuarryEnergyConfigAccessor implements EnergyConfigAccessor {
    INSTANCE;

    @Override
    public double makeFrame() {
        return QuarryPlus.config.power.quarryEnergyMakeFrame;
    }

    @Override
    public double moveHead() {
        return QuarryPlus.config.power.quarryEnergyMoveHead;
    }

    @Override
    public double breakBlock() {
        return QuarryPlus.config.power.quarryEnergyBreakBlock;
    }

    @Override
    public double removeFluid() {
        return QuarryPlus.config.power.quarryEnergyRemoveFluid;
    }
}
