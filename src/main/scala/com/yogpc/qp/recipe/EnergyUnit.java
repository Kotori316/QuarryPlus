package com.yogpc.qp.recipe;

public enum EnergyUnit {
    RF(1), MJ(0.1);
    private final double oneRf;

    EnergyUnit(double oneRf) {
        this.oneRf = oneRf;
    }

    public double multiple() {
        return this.oneRf;
    }
}
