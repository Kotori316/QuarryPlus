package com.yogpc.qp.forge;

import com.yogpc.qp.QuarryConfig;
import com.yogpc.qp.machine.PowerMap;
import net.minecraftforge.fml.loading.FMLEnvironment;

public final class QuarryConfigForge implements QuarryConfig {
    @Override
    public boolean isDebug() {
        return !FMLEnvironment.production;
    }

    @Override
    public PowerMap getPowerMap() {
        return new PowerMap(PowerMap.Default.QUARRY);
    }
}
