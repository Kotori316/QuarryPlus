package com.yogpc.qp.machine;

public interface MachineStorageFactory {
    MachineStorage createMachineStorage();

    class Default implements MachineStorageFactory {
        @Override
        public MachineStorage createMachineStorage() {
            return new MachineStorage();
        }
    }
}
