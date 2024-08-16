package com.yogpc.qp.machine.quarry;

import com.yogpc.qp.machine.module.ModuleInventory;
import com.yogpc.qp.machine.module.ModuleInventoryHolder;

public final class QuarryModuleInventoryHolder implements ModuleInventoryHolder<QuarryEntity> {
    @Override
    public ModuleInventory getModuleInventory(QuarryEntity instance) {
        return instance.moduleInventory;
    }

    @Override
    public Class<QuarryEntity> supportingClass() {
        return QuarryEntity.class;
    }
}
