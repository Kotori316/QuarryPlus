package com.yogpc.qp.fabric;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class LoadTest implements FabricGameTest {
    @GameTest(template = EMPTY_STRUCTURE)
    public void load(GameTestHelper helper) {
        helper.assertValueEqual("QuarryPlus".toLowerCase(Locale.ROOT), QuarryPlus.modID, "ModId");

        helper.assertValueEqual("Fabric", PlatformAccess.getAccess().platformName(), "PlatformName");
        assertEquals("Fabric", new PlatformAccessFabric().platformName(), "PlatformName");

        helper.succeed();
    }
}
