package com.yogpc.qp;

import com.yogpc.qp.machines.marker.Screen16Marker;
import com.yogpc.qp.machines.marker.ScreenFlexMarker;
import com.yogpc.qp.machines.misc.YSetterScreen;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.render.Render16Marker;
import com.yogpc.qp.render.RenderFlexMarker;
import com.yogpc.qp.render.RenderMarker;
import com.yogpc.qp.render.RenderQuarry;
import com.yogpc.qp.render.Sprites;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.minecraft.client.renderer.RenderType;

public class QuarryPlusClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        QuarryPlus.LOGGER.info("Client init is called. {} ", QuarryPlus.modID);
        PacketHandler.Client.initClient();
        BlockRenderLayerMap.INSTANCE.putBlock(QuarryPlus.ModObjects.BLOCK_FRAME, RenderType.cutoutMipped());
        BlockRenderLayerMap.INSTANCE.putBlock(QuarryPlus.ModObjects.BLOCK_DUMMY, RenderType.translucent());
        Sprites.register();

        BlockEntityRendererRegistry.register(QuarryPlus.ModObjects.MARKER_TYPE, RenderMarker::new);
        BlockEntityRendererRegistry.register(QuarryPlus.ModObjects.QUARRY_TYPE, RenderQuarry::new);
        BlockEntityRendererRegistry.register(QuarryPlus.ModObjects.FLEX_MARKER_TYPE, RenderFlexMarker::new);
        BlockEntityRendererRegistry.register(QuarryPlus.ModObjects.MARKER_16_TYPE, Render16Marker::new);

        ScreenRegistry.register(QuarryPlus.ModObjects.Y_SETTER_HANDLER_TYPE, YSetterScreen::new);
        ScreenRegistry.register(QuarryPlus.ModObjects.FLEX_MARKER_HANDLER_TYPE, ScreenFlexMarker::new);
        ScreenRegistry.register(QuarryPlus.ModObjects.MARKER_16_HANDLER_TYPE, Screen16Marker::new);
    }
}
