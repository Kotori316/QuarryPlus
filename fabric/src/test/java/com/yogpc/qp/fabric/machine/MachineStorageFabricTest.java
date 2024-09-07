package com.yogpc.qp.fabric.machine;

import com.yogpc.qp.machine.MachineStorage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class MachineStorageFabricTest {
    @Test
    void instance() {
        var storage = MachineStorage.of();
        assertInstanceOf(MachineStorageFabric.class, storage);
    }
}
