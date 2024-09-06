package com.yogpc.qp.machine.storage;

import com.yogpc.qp.machine.MachineStorage;
import com.yogpc.qp.machine.MachineStorageHolder;

public final class DebugStorageHolder implements MachineStorageHolder<DebugStorageEntity> {
    @Override
    public MachineStorage getMachineStorage(DebugStorageEntity instance) {
        return instance.storage;
    }

    @Override
    public Class<DebugStorageEntity> supportingClass() {
        return DebugStorageEntity.class;
    }
}
