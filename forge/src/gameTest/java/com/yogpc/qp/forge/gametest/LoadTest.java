package com.yogpc.qp.forge.gametest;

import com.kotori316.testutil.common.TestFunction;
import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.forge.PlatformAccessForge;
import com.yogpc.qp.gametest.GameTestFunctions;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraftforge.gametest.GameTest;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@SuppressWarnings("unused")
public final class LoadTest {

    @GameTest()
    public void load(GameTestHelper helper) {
        helper.assertValueEqual("QuarryPlus".toLowerCase(Locale.ROOT), QuarryPlus.modID, Component.literal("ModId"));

        assertEquals("Forge", new PlatformAccessForge().platformName(), "PlatformName");
        assertInstanceOf(PlatformAccessForge.class, PlatformAccess.getAccess());

        helper.succeed();
    }

    public List<TestFunction> commonTests() {
        // Use modId as batch name
        return GameTestFunctions.createTestFunctionsNoPlace(QuarryPlus.modID + ":test", "minecraft:empty");
    }

    public List<TestFunction> placeTests() {
        return GameTestFunctions.createTestFunctionsPlace(QuarryPlus.modID + ":test", QuarryPlus.modID + ":" + "empty");
    }
}
