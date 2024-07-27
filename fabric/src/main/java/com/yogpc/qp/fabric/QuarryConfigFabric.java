package com.yogpc.qp.fabric;

import com.yogpc.qp.QuarryConfig;
import com.yogpc.qp.fabric.machine.PowerMapFabric;
import com.yogpc.qp.machine.PowerMap;
import net.fabricmc.loader.api.FabricLoader;

public final class QuarryConfigFabric implements QuarryConfig {
    @Override
    public boolean isDebug() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public PowerMap getPowerMap() {
        return new PowerMapFabric();
    }
}
