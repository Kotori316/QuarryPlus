package com.yogpc.qp.fabric.machine;

import com.yogpc.qp.machine.PowerMap;

public final class PowerMapFabric implements PowerMap {
    @Override
    public Quarry quarry() {
        return Default.QUARRY;
    }
}
