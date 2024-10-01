package com.yogpc.qp.machine.advquarry;

import com.yogpc.qp.machine.module.ModuleInventory;
import com.yogpc.qp.machine.module.ModuleInventoryHolder;

public final class AdvQuarryModuleInventoryHolder implements ModuleInventoryHolder<AdvQuarryEntity> {
    @Override
    public ModuleInventory getModuleInventory(AdvQuarryEntity instance) {
        return instance.moduleInventory;
    }

    @Override
    public Class<AdvQuarryEntity> supportingClass() {
        return AdvQuarryEntity.class;
    }
}
