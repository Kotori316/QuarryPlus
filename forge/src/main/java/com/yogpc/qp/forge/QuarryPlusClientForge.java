package com.yogpc.qp.forge;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.advpump.AdvPumpScreen;
import com.yogpc.qp.machine.advquarry.AdvQuarryScreen;
import com.yogpc.qp.machine.marker.ChunkMarkerScreen;
import com.yogpc.qp.machine.marker.FlexibleMarkerScreen;
import com.yogpc.qp.machine.misc.YSetterScreen;
import com.yogpc.qp.machine.module.FilterModuleScreen;
import com.yogpc.qp.machine.module.ModuleScreen;
import com.yogpc.qp.machine.mover.MoverScreen;
import com.yogpc.qp.machine.placer.PlacerScreen;
import com.yogpc.qp.machine.placer.RemotePlacerScreen;
import com.yogpc.qp.machine.storage.DebugStorageScreen;
import com.yogpc.qp.render.*;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

public final class QuarryPlusClientForge {
    static void registerClientBus(BusGroup modBus) {
        FMLClientSetupEvent.getBus(modBus).addListener(QuarryPlusClientForge::clientInit);
    }

    @SubscribeEvent
    public static void clientInit(FMLClientSetupEvent event) {
        QuarryPlus.LOGGER.info("Initialize Client");
        BlockEntityRenderers.register(PlatformAccessForge.RegisterObjectsForge.QUARRY_ENTITY_TYPE.get(), RenderQuarry::new);
        BlockEntityRenderers.register(PlatformAccessForge.RegisterObjectsForge.MARKER_ENTITY_TYPE.get(), RenderMarker::new);
        BlockEntityRenderers.register(PlatformAccessForge.RegisterObjectsForge.FLEXIBLE_MARKER_ENTITY_TYPE.get(), RenderFlexibleMarker::new);
        BlockEntityRenderers.register(PlatformAccessForge.RegisterObjectsForge.CHUNK_MARKER_ENTITY_TYPE.get(), RenderChunkMarker::new);
        BlockEntityRenderers.register(PlatformAccessForge.RegisterObjectsForge.ADV_QUARRY_ENTITY_TYPE.get(), RenderAdvQuarry::new);
        MenuScreens.register(PlatformAccessForge.RegisterObjectsForge.Y_SET_MENU_TYPE.get(), YSetterScreen::new);
        MenuScreens.register(PlatformAccessForge.RegisterObjectsForge.MOVER_MENU_TYPE.get(), MoverScreen::new);
        MenuScreens.register(PlatformAccessForge.RegisterObjectsForge.MODULE_MENU_TYPE.get(), ModuleScreen::new);
        MenuScreens.register(PlatformAccessForge.RegisterObjectsForge.FLEXIBLE_MARKER_MENU_TYPE.get(), FlexibleMarkerScreen::new);
        MenuScreens.register(PlatformAccessForge.RegisterObjectsForge.CHUNK_MARKER_MENU_TYPE.get(), ChunkMarkerScreen::new);
        MenuScreens.register(PlatformAccessForge.RegisterObjectsForge.DEBUG_STORAGE_MENU_TYPE.get(), DebugStorageScreen::new);
        MenuScreens.register(PlatformAccessForge.RegisterObjectsForge.ADV_QUARRY_MENU_TYPE.get(), AdvQuarryScreen::new);
        MenuScreens.register(PlatformAccessForge.RegisterObjectsForge.ADV_PUMP_MENU_TYPE.get(), AdvPumpScreen::new);
        MenuScreens.register(PlatformAccessForge.RegisterObjectsForge.FILTER_MODULE_MENU_TYPE.get(), FilterModuleScreen::new);
        MenuScreens.register(PlatformAccessForge.RegisterObjectsForge.PLACER_MENU_TYPE.get(), PlacerScreen::new);
        MenuScreens.register(PlatformAccessForge.RegisterObjectsForge.REMOTE_PLACER_MENU_TYPE.get(), RemotePlacerScreen::new);
        setRenderLayer();
        QuarryPlus.LOGGER.info("Initialize Client finished");
    }

    private static void setRenderLayer() {
        ItemBlockRenderTypes.setRenderLayer(PlatformAccessForge.RegisterObjectsForge.BLOCK_FRAME.get(), ChunkSectionLayer.CUTOUT);
        ItemBlockRenderTypes.setRenderLayer(PlatformAccessForge.RegisterObjectsForge.BLOCK_SOFT.get(), ChunkSectionLayer.CUTOUT);
    }
}
