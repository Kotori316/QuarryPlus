package com.yogpc.qp.forge.gametest;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.forge.PlatformAccessForge;
import com.yogpc.qp.gametest.GameTestFunctions;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraftforge.gametest.GameTestHolder;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@SuppressWarnings("unused")
@GameTestHolder(QuarryPlus.modID)
public final class LoadTest {
    private static final String STRUCTURE = "no_place";
    private static final String STRUCTURE_MOD_ID = QuarryPlus.modID + ":" + QuarryPlus.modID + "." + STRUCTURE;

    @GameTest(template = STRUCTURE)
    public void load(GameTestHelper helper) {
        helper.assertValueEqual("QuarryPlus".toLowerCase(Locale.ROOT), QuarryPlus.modID, "ModId");

        assertEquals("Forge", new PlatformAccessForge().platformName(), "PlatformName");
        assertInstanceOf(PlatformAccessForge.class, PlatformAccess.getAccess());

        helper.succeed();
    }

    @GameTestGenerator
    public List<TestFunction> commonTests() {
        // Use modId as batch name
        return GameTestFunctions.createTestFunctions(QuarryPlus.modID, STRUCTURE_MOD_ID);
    }
}
