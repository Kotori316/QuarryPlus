package com.yogpc.qp.machine;

import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

public interface MachineStorageHolder<T> {
    MachineStorage getMachineStorage(T instance);

    Class<T> supportingClass();

    /**
     * Service providers are declared in static resource files, so they never change at runtime.
     * Resolving them once avoids a {@link ServiceLoader} scan (class loading + service file parsing)
     * on every invocation.
     */
    final class Providers {
        private Providers() {
        }

        static final List<MachineStorageHolder<?>> HOLDERS =
            ServiceLoader.load(MachineStorageHolder.class).stream()
                .map(ServiceLoader.Provider::get)
                .toList();
    }

    @SuppressWarnings("unchecked")
    static <T> Optional<MachineStorageHolder<T>> getHolder(T object) {
        if (object == null) {
            return Optional.empty();
        }

        for (MachineStorageHolder<?> holder : Providers.HOLDERS) {
            if (holder.supportingClass().isAssignableFrom(object.getClass())) {
                return Optional.of((MachineStorageHolder<T>) holder);
            }
        }
        return Optional.empty();
    }

    record Constant(MachineStorage storage) {
    }

    class ForConstant implements MachineStorageHolder<Constant> {
        @Override
        public MachineStorage getMachineStorage(Constant instance) {
            return instance.storage;
        }

        @Override
        public Class<Constant> supportingClass() {
            return Constant.class;
        }
    }
}
