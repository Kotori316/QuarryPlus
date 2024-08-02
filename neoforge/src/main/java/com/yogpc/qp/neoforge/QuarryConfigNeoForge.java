package com.yogpc.qp.neoforge;

import com.yogpc.qp.QuarryConfig;
import com.yogpc.qp.machine.PowerMap;
import net.neoforged.fml.loading.FMLEnvironment;

public final class QuarryConfigNeoForge implements QuarryConfig {
    @Override
    public boolean isDebug() {
        return !FMLEnvironment.production;
    }

    @Override
    public PowerMap getPowerMap() {
        return new PowerMap(PowerMap.Default.QUARRY);
    }
}
