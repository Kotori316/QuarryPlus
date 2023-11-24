package com.yogpc.qp.gametest;

import java.util.function.Consumer;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;

final class Utils {
    static TestFunction create(String name, Consumer<GameTestHelper> test) {
        return new TestFunction(
            "defaultBatch", name, FabricGameTest.EMPTY_STRUCTURE, 100, 0L,
            true, test
        );
    }

    static void assertTrue(boolean condition, String message) {
        if (!condition)
            throw new GameTestAssertException(message);
    }

    static <T> T assertNotNull(T o) {
        if (o == null) {
            throw new GameTestAssertException("Object must not be null.");
        }
        return o;
    }
}
