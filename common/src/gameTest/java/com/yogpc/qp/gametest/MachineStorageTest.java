package com.yogpc.qp.gametest;

import com.yogpc.qp.machine.MachineStorage;
import net.minecraft.gametest.framework.GameTestHelper;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public final class MachineStorageTest {
    public static void create(GameTestHelper helper) {
        var storage = assertDoesNotThrow(MachineStorage::of);
        assertNotNull(storage);
        helper.succeed();
    }
}
