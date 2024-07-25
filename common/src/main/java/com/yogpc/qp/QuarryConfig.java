package com.yogpc.qp;

import com.yogpc.qp.machine.PowerMap;

public interface QuarryConfig {
    PowerMap getPowerMap();

    default double rebornEnergyConversionCoefficient() {
        return 1d / 16d;
    }
}
