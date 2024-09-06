package com.yogpc.qp.machine;

import java.util.Optional;
import java.util.ServiceLoader;

public interface MachineStorageHolder<T> {
    MachineStorage getMachineStorage(T instance);

    Class<T> supportingClass();

    @SuppressWarnings("unchecked")
    static <T> Optional<MachineStorageHolder<T>> getHolder(T object) {
        if (object == null) {
            return Optional.empty();
        }

        for (MachineStorageHolder<?> holder : ServiceLoader.load(MachineStorageHolder.class)) {
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
