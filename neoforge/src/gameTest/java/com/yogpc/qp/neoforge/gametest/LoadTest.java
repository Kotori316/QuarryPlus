package com.yogpc.qp.neoforge.gametest;

import com.kotori316.testutil.common.TestFunction;
import com.kotori316.testutil.common.TestFunctionRegister;
import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.gametest.GameTestFunctions;
import com.yogpc.qp.neoforge.PlatformAccessNeoForge;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@EventBusSubscriber(modid = QuarryPlus.modID)
public final class LoadTest {
    static {
        QuarryPlus.LOGGER.info("Loading GameTest for NeoForge");
    }

    @SubscribeEvent
    public static void registerGameTest(FMLConstructModEvent event) {
        var tests = Stream.of(
            commonTests().stream(),
            placeTests().stream(),
            MachineEnergyHandlerTest.tests().stream(),
            MachineStorageHandlerTest.tests().stream(),
            Stream.of(TestFunction.create(QuarryPlus.modID, "load", LoadTest::load))
        ).flatMap(Function.identity());
        tests.forEach(TestFunctionRegister::registerTestFunction);
    }

    static void load(GameTestHelper helper) {
        helper.assertValueEqual("QuarryPlus".toLowerCase(Locale.ROOT), QuarryPlus.modID, Component.literal("ModId"));

        assertEquals("NeoForge", new PlatformAccessNeoForge().platformName(), "PlatformName");
        assertInstanceOf(PlatformAccessNeoForge.class, PlatformAccess.getAccess());

        helper.succeed();
    }

    static List<TestFunction> commonTests() {
        return GameTestFunctions.createTestFunctionsNoPlace(QuarryPlus.modID + ":test", "minecraft:empty");
    }

    static List<TestFunction> placeTests() {
        return GameTestFunctions.createTestFunctionsPlace(QuarryPlus.modID + ":test", QuarryPlus.modID + ":" + "empty");
    }
}
