package com.yogpc.qp.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.marker.BlockExMarker;
import com.yogpc.qp.machines.marker.Tile16Marker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@SuppressWarnings({"unused", "DuplicatedCode"})
public class Render16Marker implements BlockEntityRenderer<Tile16Marker> {
    public Render16Marker(BlockEntityRendererProvider.Context d) {
    }

    @Override
    public void render(Tile16Marker te, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
        Minecraft.getInstance().getProfiler().push(QuarryPlus.modID);
        Minecraft.getInstance().getProfiler().push(BlockExMarker.Block16Marker.NAME);
        BlockPos markerPos = te.getBlockPos();
        var buffer = vertexConsumers.getBuffer(RenderType.cutoutMipped());
        matrices.pushPose();
        matrices.translate(-markerPos.getX(), -markerPos.getY(), -markerPos.getZ());
        if (te.boxes != null) {
            for (Box box : te.boxes) {
                if (box != null)
                    box.render(buffer, matrices, Sprites.INSTANCE.getWhite(), ColorBox.redColor);
            }
        }
        matrices.popPose();
        Minecraft.getInstance().getProfiler().pop();
        Minecraft.getInstance().getProfiler().pop();
    }

    @Override
    public boolean shouldRenderOffScreen(Tile16Marker blockEntity) {
        return true;
    }
}
