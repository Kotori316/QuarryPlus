package com.yogpc.qp.fabric;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.fabric.packet.PacketHandler;
import com.yogpc.qp.machine.misc.YSetterScreen;
import com.yogpc.qp.machine.mover.MoverScreen;
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
        MenuScreens.register(PlatformAccessFabric.RegisterObjectsFabric.Y_SET_MENU, YSetterScreen::new);
        MenuScreens.register(PlatformAccessFabric.RegisterObjectsFabric.MOVER_MENU, MoverScreen::new);
        QuarryPlus.LOGGER.info("Initialize Client finished");
    }
}
