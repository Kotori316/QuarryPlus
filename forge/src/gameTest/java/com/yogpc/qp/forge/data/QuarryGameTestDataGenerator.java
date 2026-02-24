package com.yogpc.qp.forge.data;

import com.yogpc.qp.QuarryPlus;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = QuarryPlus.modID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class QuarryGameTestDataGenerator {
    @SubscribeEvent
    public static void onEvent(GatherDataEvent event) {
        var testEnvironmentGenerator = new TestEnvironmentGeneratorForge(event.getGenerator().getPackOutput(), event.getLookupProvider());
        event.getGenerator().addProvider(event.includeServer(), testEnvironmentGenerator);
        event.getGenerator().addProvider(event.includeServer(), new TestInstanceGeneratorForge(event.getGenerator().getPackOutput(), testEnvironmentGenerator.getPatchedLookupProvider()));
    }
}
