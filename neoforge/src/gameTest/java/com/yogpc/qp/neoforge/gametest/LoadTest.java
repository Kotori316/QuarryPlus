package com.yogpc.qp.neoforge.gametest;

import com.kotori316.testutil.common.TestFunction;
import com.kotori316.testutil.common.TestUtilityCommon;
import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.gametest.GameTestFunctions;
import com.yogpc.qp.neoforge.PlatformAccessNeoForge;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@EventBusSubscriber(modid = QuarryPlus.modID)
public final class LoadTest {
    static {
        QuarryPlus.LOGGER.info("Loading GameTest for NeoForge");
    }

    private static final List<TestFunction> TESTS = new ArrayList<>();

    @SubscribeEvent
    public static void registerGameTest(FMLConstructModEvent event) {
        QuarryPlus.LOGGER.info("Registering GameTest for NeoForge");
        var tests = Stream.of(
            commonTests().stream(),
            placeTests().stream(),
            MachineEnergyHandlerTest.tests().stream(),
            MachineStorageNeoForgeTest.tests().stream(),
            AdvPumpFluidHandlerTest.tests().stream(),
            Stream.of(TestFunction.create(QuarryPlus.modID, "load", LoadTest::load))
        ).flatMap(Function.identity()).toList();
        TESTS.addAll(tests);
        // tests.forEach(TestFunctionRegister::registerTestFunction);
        QuarryPlus.LOGGER.info("Registered GameTest for NeoForge");
    }

    @SubscribeEvent
    public static void registerTestFunctions(RegisterEvent event) {
        event.register(Registries.TEST_FUNCTION, consumerRegisterHelper ->
            TESTS.forEach(t -> consumerRegisterHelper.register(t.name(), t.test()))
        );
    }

    @SubscribeEvent
    public static void registerTests(RegisterGameTestsEvent event) {
        QuarryPlus.LOGGER.info("registerTests {}", event);
        Map<Identifier, @NotNull Holder<TestEnvironmentDefinition<?>>> environments = TESTS.stream().map(TestFunction::environmentName).distinct()
            .collect(Collectors.toMap(Function.identity(), event::registerEnvironment));
        TESTS.forEach(testFunction -> {
            var Identifier = testFunction.name();
            event.registerTest(Identifier, testFunction.createTestInstance(environments.get(testFunction.environmentName())));
        });
        TestUtilityCommon.TEST_LOADER_LOGGER.info("Registered {} tests for {}", TESTS.size(), QuarryPlus.modID);
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
