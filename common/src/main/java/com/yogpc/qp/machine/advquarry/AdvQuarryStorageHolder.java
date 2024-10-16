package com.yogpc.qp.machine.advquarry;

import com.yogpc.qp.machine.MachineStorage;
import com.yogpc.qp.machine.MachineStorageHolder;

public final class AdvQuarryStorageHolder implements MachineStorageHolder<AdvQuarryEntity> {
    @Override
    public MachineStorage getMachineStorage(AdvQuarryEntity instance) {
        return instance.storage;
    }

    @Override
    public Class<AdvQuarryEntity> supportingClass() {
        return AdvQuarryEntity.class;
    }
}
