package com.yogpc.qp.machines;

public interface EnergyConfigAccessor {
    EnergyConfigAccessor ONES = new Ones();

    double makeFrame();

    double moveHead();

    double breakBlock();

    double removeFluid();
}

final class Ones implements EnergyConfigAccessor {
    @Override
    public double makeFrame() {
        return 1d;
    }

    @Override
    public double moveHead() {
        return 1d;
    }

    @Override
    public double breakBlock() {
        return 1d;
    }

    @Override
    public double removeFluid() {
        return 1d;
    }
}
