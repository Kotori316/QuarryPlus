package com.yogpc.qp.fabric;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.fabric.machine.quarry.QuarryScreenFabric;
import com.yogpc.qp.fabric.packet.PacketHandler;
import com.yogpc.qp.machine.marker.ChunkMarkerScreen;
import com.yogpc.qp.machine.misc.YSetterScreen;
import com.yogpc.qp.machine.module.ModuleScreen;
import com.yogpc.qp.machine.mover.MoverScreen;
import com.yogpc.qp.render.RenderChunkMarker;
import com.yogpc.qp.render.RenderFlexibleMarker;
import com.yogpc.qp.render.RenderMarker;
import com.yogpc.qp.render.RenderQuarry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public final class QuarryPlusFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        QuarryPlus.LOGGER.info("Initialize Client");
        PacketHandler.Client.initClient();
        BlockRenderLayerMap.INSTANCE.putBlock(PlatformAccessFabric.RegisterObjectsFabric.FRAME_BLOCK, RenderType.cutoutMipped());
        BlockEntityRenderers.register(PlatformAccessFabric.RegisterObjectsFabric.QUARRY_ENTITY_TYPE, RenderQuarry::new);
        BlockEntityRenderers.register(PlatformAccessFabric.RegisterObjectsFabric.MARKER_ENTITY_TYPE, RenderMarker::new);
        BlockEntityRenderers.register(PlatformAccessFabric.RegisterObjectsFabric.FLEXIBLE_MARKER_ENTITY_TYPE, RenderFlexibleMarker::new);
        BlockEntityRenderers.register(PlatformAccessFabric.RegisterObjectsFabric.CHUNK_MARKER_ENTITY_TYPE, RenderChunkMarker::new);
        MenuScreens.register(PlatformAccessFabric.RegisterObjectsFabric.QUARRY_MENU, QuarryScreenFabric::new);
        MenuScreens.register(PlatformAccessFabric.RegisterObjectsFabric.Y_SET_MENU, YSetterScreen::new);
        MenuScreens.register(PlatformAccessFabric.RegisterObjectsFabric.MOVER_MENU, MoverScreen::new);
        MenuScreens.register(PlatformAccessFabric.RegisterObjectsFabric.MODULE_MENU, ModuleScreen::new);
        MenuScreens.register(PlatformAccessFabric.RegisterObjectsFabric.CHUNK_MARKER_MENU, ChunkMarkerScreen::new);
        QuarryPlus.LOGGER.info("Initialize Client finished");
    }
}
