package com.yogpc.qp.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.marker.BlockExMarker;
import com.yogpc.qp.machines.marker.TileFlexMarker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
@SuppressWarnings({"unused", "DuplicatedCode"})
public class RenderFlexMarker implements BlockEntityRenderer<TileFlexMarker> {

    public RenderFlexMarker(BlockEntityRendererProvider.Context d) {
    }

    @Override
    public void render(TileFlexMarker te, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
        Minecraft.getInstance().getProfiler().push(QuarryPlus.modID);
        Minecraft.getInstance().getProfiler().push(BlockExMarker.BlockFlexMarker.NAME);
        BlockPos markerPos = te.getBlockPos();
        var buffer = vertexConsumers.getBuffer(RenderType.cutoutMipped());
        matrices.pushPose();
        matrices.translate(-markerPos.getX(), -markerPos.getY(), -markerPos.getZ());
        if (te.boxes != null) {
            for (Box box : te.boxes) {
                box.render(buffer, matrices, Sprites.INSTANCE.getWhite(), ColorBox.redColor);
            }
        }
        if (te.directionBox != null) {
            te.directionBox.render(buffer, matrices, Sprites.INSTANCE.getWhite(), ColorBox.blueColor);
        }
        matrices.popPose();
        Minecraft.getInstance().getProfiler().pop();
        Minecraft.getInstance().getProfiler().pop();
    }

    @Override
    public boolean shouldRenderOffScreen(TileFlexMarker blockEntity) {
        return true;
    }
}
