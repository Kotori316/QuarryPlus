package com.yogpc.qp.machine.advpump;

import com.yogpc.qp.machine.module.ModuleInventory;
import com.yogpc.qp.machine.module.ModuleInventoryHolder;

public final class AdvPumpModuleInventoryHolder implements ModuleInventoryHolder<AdvPumpEntity> {
    @Override
    public ModuleInventory getModuleInventory(AdvPumpEntity instance) {
        return instance.moduleInventory;
    }

    @Override
    public Class<AdvPumpEntity> supportingClass() {
        return AdvPumpEntity.class;
    }
}
