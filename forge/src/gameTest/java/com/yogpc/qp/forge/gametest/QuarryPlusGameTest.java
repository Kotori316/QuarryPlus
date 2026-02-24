package com.yogpc.qp.forge.gametest;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.forge.data.GatherGameTest;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.StructureUtils;
import net.minecraftforge.registries.RegisterEvent;

import java.nio.file.Path;
import java.util.ServiceLoader;

@SuppressWarnings("unused") // Via reflection from QuarryPlusForge
public final class QuarryPlusGameTest {

    public static void register(RegisterEvent event) {
        if (event.getRegistryKey().equals(Registries.TEST_FUNCTION)) {
            registerTests(event);
            StructureUtils.testStructuresDir = Path.of("gameteststructures");
        }
    }

    private static void registerTests(RegisterEvent event) {
        ServiceLoader<GatherGameTest> loader = ServiceLoader.load(GatherGameTest.class);
        int serviceLoaderCount = 0;
        int testCount = 0;
        for (GatherGameTest gatherGameTest : loader) {
            serviceLoaderCount++;
            for (var property : gatherGameTest.gather()) {
                testCount++;
                event.register(Registries.TEST_FUNCTION, property.id(), property::test);
            }
        }
        QuarryPlus.LOGGER.info("Loading GameTest for Forge, found {} services with {} tests", serviceLoaderCount, testCount);
    }
}
