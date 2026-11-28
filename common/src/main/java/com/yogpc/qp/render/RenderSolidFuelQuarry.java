package com.yogpc.qp.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class RenderSolidFuelQuarry extends RenderQuarry {
    private static final ColorBox color = new ColorBox(0xD0, 0xD0, 0xD0, 0xFF);

    public RenderSolidFuelQuarry(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    Buffer getBuffer(VertexConsumer vertexConsumer, PoseStack.Pose matrices) {
        return new Buffer(vertexConsumer, matrices, color);
    }
}
