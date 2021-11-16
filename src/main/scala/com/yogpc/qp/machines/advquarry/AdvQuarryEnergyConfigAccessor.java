package com.yogpc.qp.machines.advquarry;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.EnergyConfigAccessor;

enum AdvQuarryEnergyConfigAccessor implements EnergyConfigAccessor {
    INSTANCE;

    @Override
    public double makeFrame() {
        return QuarryPlus.config.adv_quarry.advQuarryEnergyMakeFrame;
    }

    @Override
    public double moveHead() {
        return QuarryPlus.config.adv_quarry.advQuarryEnergyMoveHead;
    }

    @Override
    public double breakBlock() {
        return QuarryPlus.config.adv_quarry.advQuarryEnergyBreakBlock;
    }

    @Override
    public double removeFluid() {
        return QuarryPlus.config.adv_quarry.advQuarryEnergyRemoveFluid;
    }
}
