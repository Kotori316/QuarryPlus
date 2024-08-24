package com.yogpc.qp.forge;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.marker.ChunkMarkerScreen;
import com.yogpc.qp.machine.misc.YSetterScreen;
import com.yogpc.qp.machine.module.ModuleScreen;
import com.yogpc.qp.machine.mover.MoverScreen;
import com.yogpc.qp.render.RenderChunkMarker;
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
        BlockEntityRenderers.register(PlatformAccessForge.RegisterObjectsForge.CHUNK_MARKER_ENTITY_TYPE.get(), RenderChunkMarker::new);
        MenuScreens.register(PlatformAccessForge.RegisterObjectsForge.Y_SET_MENU_TYPE.get(), YSetterScreen::new);
        MenuScreens.register(PlatformAccessForge.RegisterObjectsForge.MOVER_MENU_TYPE.get(), MoverScreen::new);
        MenuScreens.register(PlatformAccessForge.RegisterObjectsForge.MODULE_MENU_TYPE.get(), ModuleScreen::new);
        MenuScreens.register(PlatformAccessForge.RegisterObjectsForge.CHUNK_MARKER_MENU_TYPE.get(), ChunkMarkerScreen::new);
        QuarryPlus.LOGGER.info("Initialize Client finished");
    }
}
