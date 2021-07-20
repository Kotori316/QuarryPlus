package com.yogpc.qp;

import com.yogpc.qp.machines.misc.YSetterScreen;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.render.RenderMarker;
import com.yogpc.qp.render.RenderQuarry;
import com.yogpc.qp.render.Sprites;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.rendereregistry.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.minecraft.client.render.RenderLayer;

public class QuarryPlusClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        QuarryPlus.LOGGER.info("Client init is called. {} ", QuarryPlus.modID);
        PacketHandler.Client.initClient();
        BlockRenderLayerMap.INSTANCE.putBlock(QuarryPlus.ModObjects.BLOCK_FRAME, RenderLayer.getCutoutMipped());
        Sprites.register();

        BlockEntityRendererRegistry.INSTANCE.register(QuarryPlus.ModObjects.MARKER_TYPE, RenderMarker::new);
        BlockEntityRendererRegistry.INSTANCE.register(QuarryPlus.ModObjects.QUARRY_TYPE, RenderQuarry::new);

        ScreenRegistry.register(QuarryPlus.ModObjects.Y_SETTER_HANDLER_TYPE, YSetterScreen::new);
    }
}
