package com.yogpc.qp.machines.quarry;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.EnergyConfigAccessor;

enum QuarryEnergyConfigAccessor implements EnergyConfigAccessor {
    INSTANCE;

    @Override
    public double makeFrame() {
        return QuarryPlus.config.quarry.quarryEnergyMakeFrame;
    }

    @Override
    public double moveHead() {
        return QuarryPlus.config.quarry.quarryEnergyMoveHead;
    }

    @Override
    public double breakBlock() {
        return QuarryPlus.config.quarry.quarryEnergyBreakBlock;
    }

    @Override
    public double removeFluid() {
        return QuarryPlus.config.quarry.quarryEnergyRemoveFluid;
    }
}
