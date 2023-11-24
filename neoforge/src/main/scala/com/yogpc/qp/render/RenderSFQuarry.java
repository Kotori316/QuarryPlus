package com.yogpc.qp.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public final class RenderSFQuarry extends RenderQuarry {
    private static final ColorBox color = new ColorBox(0xD0, 0xD0, 0xD0, 0xFF);

    public RenderSFQuarry(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    Buffer getBuffer(MultiBufferSource vertexConsumers, PoseStack matrices) {
        return new Buffer(vertexConsumers.getBuffer(RenderType.cutout()), matrices, color);
    }
}
