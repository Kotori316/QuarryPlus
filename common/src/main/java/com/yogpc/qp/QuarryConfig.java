package com.yogpc.qp;

import com.yogpc.qp.machine.PowerMap;

public interface QuarryConfig {
    boolean debug();

    PowerMap powerMap();

    double rebornEnergyConversionCoefficient();
}
