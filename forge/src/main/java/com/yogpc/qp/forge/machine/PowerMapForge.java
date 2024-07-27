package com.yogpc.qp.forge.machine;

import com.yogpc.qp.machine.PowerMap;

public final class PowerMapForge implements PowerMap {
    @Override
    public Quarry quarry() {
        return Default.QUARRY;
    }
}
