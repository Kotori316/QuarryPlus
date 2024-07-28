package com.yogpc.qp.neoforge.machine;

import com.yogpc.qp.machine.PowerMap;

public final class PowerMapNeoForge implements PowerMap {
    @Override
    public Quarry quarry() {
        return Default.QUARRY;
    }
}
