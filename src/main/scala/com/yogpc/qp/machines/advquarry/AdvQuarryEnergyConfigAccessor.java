package com.yogpc.qp.machines.advquarry;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.EnergyConfigAccessor;

enum AdvQuarryEnergyConfigAccessor implements EnergyConfigAccessor {
    INSTANCE;

    @Override
    public double makeFrame() {
        return QuarryPlus.config.power.advQuarryEnergyMakeFrame;
    }

    @Override
    public double moveHead() {
        return QuarryPlus.config.power.advQuarryEnergyMoveHead;
    }

    @Override
    public double breakBlock() {
        return QuarryPlus.config.power.advQuarryEnergyBreakBlock;
    }

    @Override
    public double removeFluid() {
        return QuarryPlus.config.power.advQuarryEnergyRemoveFluid;
    }
}
