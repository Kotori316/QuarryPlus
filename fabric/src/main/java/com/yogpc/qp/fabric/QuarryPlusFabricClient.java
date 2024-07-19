package com.yogpc.qp.fabric;

import com.yogpc.qp.QuarryPlus;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.renderer.RenderType;

public final class QuarryPlusFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        QuarryPlus.LOGGER.info("Initialize Client");
        BlockRenderLayerMap.INSTANCE.putBlock(PlatformAccessFabric.RegisterObjectsFabric.FRAME_BLOCK, RenderType.cutoutMipped());
        QuarryPlus.LOGGER.info("Initialize Client finished");
    }
}
