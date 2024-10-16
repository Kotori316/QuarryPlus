package com.yogpc.qp.machine.quarry;

import com.yogpc.qp.machine.MachineStorage;
import com.yogpc.qp.machine.MachineStorageHolder;

public final class QuarryStorageHolder implements MachineStorageHolder<QuarryEntity> {
    @Override
    public MachineStorage getMachineStorage(QuarryEntity instance) {
        return instance.storage;
    }

    @Override
    public Class<QuarryEntity> supportingClass() {
        return QuarryEntity.class;
    }
}
