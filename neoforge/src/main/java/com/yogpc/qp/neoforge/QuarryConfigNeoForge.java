package com.yogpc.qp.neoforge;

import com.yogpc.qp.QuarryConfig;
import com.yogpc.qp.machine.PowerMap;
import net.neoforged.fml.loading.FMLEnvironment;

public final class QuarryConfigNeoForge implements QuarryConfig {
    @Override
    public boolean debug() {
        return !FMLEnvironment.production;
    }

    @Override
    public PowerMap powerMap() {
        return new PowerMap(PowerMap.Default.QUARRY);
    }

    @Override
    public double rebornEnergyConversionCoefficient() {
        return 0;
    }
}
