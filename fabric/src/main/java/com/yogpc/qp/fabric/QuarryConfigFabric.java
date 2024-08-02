package com.yogpc.qp.fabric;

import com.yogpc.qp.QuarryConfig;
import com.yogpc.qp.machine.PowerMap;
import net.fabricmc.loader.api.FabricLoader;

public final class QuarryConfigFabric implements QuarryConfig {
    @Override
    public boolean debug() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public PowerMap powerMap() {
        return new PowerMap(PowerMap.Default.QUARRY);
    }
}
