package com.yogpc.qp.neoforge.gametest;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.gametest.GameTestFunctions;
import com.yogpc.qp.neoforge.PlatformAccessNeoForge;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@PrefixGameTestTemplate(value = false)
public final class LoadTest {
    private static final String STRUCTURE = "no_place";
    private static final String STRUCTURE_MOD_ID = QuarryPlus.modID + ":" + STRUCTURE;

    @EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD, modid = QuarryPlus.modID)
    public static final class Register {
        @SubscribeEvent
        public static void registerGameTest(RegisterGameTestsEvent event) {
            event.register(LoadTest.class);
        }
    }

    @GameTest(template = STRUCTURE)
    public void load(GameTestHelper helper) {
        helper.assertValueEqual("QuarryPlus".toLowerCase(Locale.ROOT), QuarryPlus.modID, "ModId");

        assertEquals("NeoForge", new PlatformAccessNeoForge().platformName(), "PlatformName");
        assertInstanceOf(PlatformAccessNeoForge.class, PlatformAccess.getAccess());

        helper.succeed();
    }

    @GameTestGenerator
    public List<TestFunction> commonTests() {
        return GameTestFunctions.createTestFunctionsNoPlace("defaultBatch", STRUCTURE_MOD_ID);
    }

    @GameTestGenerator
    public List<TestFunction> placeTests() {
        return GameTestFunctions.createTestFunctionsPlace("defaultBatch", QuarryPlus.modID + ":" + "empty");
    }
}
