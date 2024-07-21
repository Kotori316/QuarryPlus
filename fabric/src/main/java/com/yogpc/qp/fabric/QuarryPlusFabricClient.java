package com.yogpc.qp.fabric;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.fabric.packet.PacketHandler;
import com.yogpc.qp.render.RenderQuarry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;

public final class QuarryPlusFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        QuarryPlus.LOGGER.info("Initialize Client");
        PacketHandler.Client.initClient();
        BlockRenderLayerMap.INSTANCE.putBlock(PlatformAccessFabric.RegisterObjectsFabric.FRAME_BLOCK, RenderType.cutoutMipped());
        BlockEntityRenderers.register(PlatformAccessFabric.RegisterObjectsFabric.QUARRY_ENTITY_TYPE, RenderQuarry::new);
        QuarryPlus.LOGGER.info("Initialize Client finished");
    }
}