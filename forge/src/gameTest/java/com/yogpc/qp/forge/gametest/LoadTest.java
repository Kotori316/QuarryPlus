package com.yogpc.qp.forge.gametest;

import com.kotori316.testutil.common.TestFunction;
import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.forge.PlatformAccessForge;
import com.yogpc.qp.forge.data.GatherGameTest;
import com.yogpc.qp.gametest.GameTestFunctions;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraftforge.gametest.GameTest;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@SuppressWarnings("unused")
public final class LoadTest implements GatherGameTest {

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

    @Override
    public Collection<GameTestProperty> gather() {
        return Stream.concat(
                Stream.of(
                    new GameTestProperty(Identifier.fromNamespaceAndPath(QuarryPlus.modID, "load"), GameTestProperty.empty(), this::load)
                ),
                Stream.of(
                    commonTests().stream(),
                    placeTests().stream()
                ).flatMap(Function.identity()).map(GameTestProperty::new)
            )
            .toList();
    }
}
