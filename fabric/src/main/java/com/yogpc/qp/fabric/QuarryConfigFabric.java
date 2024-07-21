package com.yogpc.qp.fabric;

import com.yogpc.qp.QuarryConfig;
import com.yogpc.qp.fabric.machine.PowerMapFabric;
import com.yogpc.qp.machine.PowerMap;

public final class QuarryConfigFabric implements QuarryConfig {
    @Override
    public PowerMap getPowerMap() {
        return new PowerMapFabric();
    }
}
