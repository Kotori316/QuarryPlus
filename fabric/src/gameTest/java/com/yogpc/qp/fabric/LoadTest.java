package com.yogpc.qp.fabric;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.gametest.GameTestFunctions;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public final class LoadTest implements FabricGameTest {
    static {
        QuarryPlus.LOGGER.info("Loading GameTest for Fabric");
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void load(GameTestHelper helper) {
        helper.assertValueEqual("QuarryPlus".toLowerCase(Locale.ROOT), QuarryPlus.modID, "ModId");

        assertEquals("Fabric", new PlatformAccessFabric().platformName(), "PlatformName");
        assertInstanceOf(PlatformAccessFabric.class, PlatformAccess.getAccess());

        helper.succeed();
    }

    @GameTestGenerator
    public List<TestFunction> commonTests() {
        return GameTestFunctions.createTestFunctionsNoPlace("defaultBatch", EMPTY_STRUCTURE);
    }

    @GameTestGenerator
    public List<TestFunction> placeTests() {
        return GameTestFunctions.createTestFunctionsPlace("defaultBatch", EMPTY_STRUCTURE);
    }
}
