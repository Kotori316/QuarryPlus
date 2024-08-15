package com.yogpc.qp.machine.module;

import java.util.Optional;
import java.util.ServiceLoader;

public interface ModuleInventoryHolder<T> {
    ModuleInventory getModuleInventory(T instance);

    Class<T> supportingClass();

    default Optional<T> cast(Object o) {
        return Optional.ofNullable(o)
            .filter(supportingClass()::isInstance)
            .map(supportingClass()::cast);
    }

    @SuppressWarnings("unchecked")
    static Optional<ModuleInventory> getFromObject(Object object) {
        if (object == null) {
            return Optional.empty();
        }

        for (ModuleInventoryHolder<Object> holder : ServiceLoader.load(ModuleInventoryHolder.class)) {
            var maybeInventory = holder.cast(object).map(holder::getModuleInventory);
            if (maybeInventory.isPresent()) {
                return maybeInventory;
            }
        }
        return Optional.empty();
    }
}
