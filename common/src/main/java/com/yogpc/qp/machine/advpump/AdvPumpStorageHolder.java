package com.yogpc.qp.machine.advpump;

import com.yogpc.qp.machine.MachineStorage;
import com.yogpc.qp.machine.MachineStorageHolder;

public final class AdvPumpStorageHolder implements MachineStorageHolder<AdvPumpEntity> {
    @Override
    public MachineStorage getMachineStorage(AdvPumpEntity instance) {
        return instance.storage;
    }

    @Override
    public Class<AdvPumpEntity> supportingClass() {
        return AdvPumpEntity.class;
    }
}
