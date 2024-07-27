package com.yogpc.qp.forge;

import com.yogpc.qp.render.RenderQuarry;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public final class QuarryPlusClientForge {
    static void registerClientBus() {
        var modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.register(QuarryPlusClientForge.class);
    }

    @SubscribeEvent
    public static void clientInit(FMLClientSetupEvent event) {
        BlockEntityRenderers.register(PlatformAccessForge.RegisterObjectsForge.QUARRY_ENTITY_TYPE.get(), RenderQuarry::new);
    }
}
