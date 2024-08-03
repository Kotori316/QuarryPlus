package com.yogpc.qp.forge;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.misc.YSetterScreen;
import com.yogpc.qp.render.RenderMarker;
import com.yogpc.qp.render.RenderQuarry;
import net.minecraft.client.gui.screens.MenuScreens;
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
        QuarryPlus.LOGGER.info("Initialize Client");
        BlockEntityRenderers.register(PlatformAccessForge.RegisterObjectsForge.QUARRY_ENTITY_TYPE.get(), RenderQuarry::new);
        BlockEntityRenderers.register(PlatformAccessForge.RegisterObjectsForge.MARKER_ENTITY_TYPE.get(), RenderMarker::new);
        MenuScreens.register(PlatformAccessForge.RegisterObjectsForge.Y_SET_MENU_TYPE.get(), YSetterScreen::new);
        QuarryPlus.LOGGER.info("Initialize Client finished");
    }
}
