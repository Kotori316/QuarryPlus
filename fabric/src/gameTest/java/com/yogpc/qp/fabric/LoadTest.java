package com.yogpc.qp.fabric;

import com.kotori316.testutil.common.TestFunctionRegister;
import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.gametest.GameTestFunctions;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;

import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public final class LoadTest implements ModInitializer {
    static {
        QuarryPlus.LOGGER.info("Loading GameTest for Fabric");
    }

    @GameTest()
    public void load(GameTestHelper helper) {
        helper.assertValueEqual("QuarryPlus".toLowerCase(Locale.ROOT), QuarryPlus.modID, Component.literal("ModId"));

        assertEquals("Fabric", new PlatformAccessFabric().platformName(), "PlatformName");
        assertInstanceOf(PlatformAccessFabric.class, PlatformAccess.getAccess());

        helper.succeed();
    }

    @Override
    public void onInitialize() {
        var tests = Stream.of(
            GameTestFunctions.createTestFunctionsNoPlace(QuarryPlus.modID + ":test", "fabric-gametest-api-v1:empty").stream(),
            GameTestFunctions.createTestFunctionsPlace(QuarryPlus.modID + ":test", "fabric-gametest-api-v1:empty").stream()
        ).flatMap(Function.identity());
        tests.forEach(TestFunctionRegister::registerTestFunction);
        TestFunctionRegister.addFunctionsToRegistry(QuarryPlus.modID, TestFunctionRegister::vanillaTestFunctionRegister);
    }
}
