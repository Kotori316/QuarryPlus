package com.yogpc.qp;

import com.yogpc.qp.machine.PowerMap;

public interface QuarryConfig {
    boolean isDebug();
    PowerMap getPowerMap();

    default double rebornEnergyConversionCoefficient() {
        return 1d / 16d;
    }
}
