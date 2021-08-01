package com.yogpc.qp.render;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.marker.BlockExMarker;
import com.yogpc.qp.machines.marker.TileFlexMarker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;

@SuppressWarnings({"unused", "DuplicatedCode"})
public class RenderFlexMarker implements BlockEntityRenderer<TileFlexMarker> {

    public RenderFlexMarker(BlockEntityRendererFactory.Context d) {
    }

    @Override
    public void render(TileFlexMarker te, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        MinecraftClient.getInstance().getProfiler().push(QuarryPlus.modID);
        MinecraftClient.getInstance().getProfiler().push(BlockExMarker.BlockFlexMarker.NAME);
        BlockPos markerPos = te.getPos();
        var buffer = vertexConsumers.getBuffer(RenderLayer.getCutoutMipped());
        matrices.push();
        matrices.translate(-markerPos.getX(), -markerPos.getY(), -markerPos.getZ());
        if (te.boxes != null) {
            for (Box box : te.boxes) {
                box.render(buffer, matrices, Sprites.INSTANCE.getWhite(), ColorBox.redColor);
            }
        }
        if (te.directionBox != null) {
            te.directionBox.render(buffer, matrices, Sprites.INSTANCE.getWhite(), ColorBox.blueColor);
        }
        matrices.pop();
        MinecraftClient.getInstance().getProfiler().pop();
        MinecraftClient.getInstance().getProfiler().pop();
    }

    @Override
    public boolean rendersOutsideBoundingBox(TileFlexMarker blockEntity) {
        return true;
    }
}
