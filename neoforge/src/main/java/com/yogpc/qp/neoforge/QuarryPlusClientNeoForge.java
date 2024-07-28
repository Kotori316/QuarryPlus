package com.yogpc.qp.neoforge;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.neoforge.render.RenderQuarryNeoForge;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

public final class QuarryPlusClientNeoForge {
    @SubscribeEvent
    public static void clientInit(FMLClientSetupEvent event) {
        QuarryPlus.LOGGER.info("Initialize Client");
        BlockEntityRenderers.register(PlatformAccessNeoForge.RegisterObjectsNeoForge.QUARRY_ENTITY_TYPE.get(), RenderQuarryNeoForge::new);
        QuarryPlus.LOGGER.info("Initialize Client finished");
    }
}
