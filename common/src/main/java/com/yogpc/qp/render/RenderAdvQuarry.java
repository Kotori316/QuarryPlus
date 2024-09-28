package com.yogpc.qp.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.Area;
import com.yogpc.qp.machine.advquarry.AdvQuarryBlock;
import com.yogpc.qp.machine.advquarry.AdvQuarryEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;

public class RenderAdvQuarry implements BlockEntityRenderer<AdvQuarryEntity> {
    @SuppressWarnings("unused")
    public RenderAdvQuarry(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(AdvQuarryEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    @SuppressWarnings("DuplicatedCode") // for readability.
    public void render(AdvQuarryEntity quarry, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
        Minecraft.getInstance().getProfiler().push(QuarryPlus.modID);

        if (quarry.renderMode().equals("frame")) {
            Minecraft.getInstance().getProfiler().push(AdvQuarryBlock.NAME);
            Area range = quarry.getArea();
            if (range != null) {
                Minecraft.getInstance().getProfiler().push("rendering");
                final double d = 1d / 16d;
                final TextureAtlasSprite sprite = Sprites.INSTANCE.getWhite();
                final ColorBox color = new ColorBox(0xFF, 0xFF, 0, 0xFF);
                VertexConsumer buffer = vertexConsumers.getBuffer(RenderType.cutout());
                BlockPos pos = quarry.getBlockPos();
                LocalPlayer player = Minecraft.getInstance().player;
                double playerX = player == null ? pos.getX() : player.getX(); //x
                double playerZ = player == null ? pos.getZ() : player.getZ(); //z
                matrices.pushPose();
                matrices.translate(-pos.getX(), -pos.getY(), -pos.getZ()); // Offset
                double startX = range.minX() + 0.5;
                double startZ = range.minZ() + 0.5;
                double endZ = range.maxZ() + 0.5;
                double endX = range.maxX() + 0.5;
                boolean b1 = Math.abs(playerZ - startZ) < 256;
                boolean b2 = Math.abs(playerZ - endZ) < 256;
                boolean b3 = Math.abs(playerX - startX) < 256;
                boolean b4 = Math.abs(playerX - endX) < 256;
                double xMin = Math.max(startX, playerX - 128);
                double xMax = Math.min(endX, playerX + 128);
                double zMin = Math.max(startZ, playerZ - 128);
                double zMax = Math.min(endZ, playerZ + 128);
                if (b1)
                    Box.apply(xMin, range.minY(), startZ, xMax, range.minY(), startZ, xMax - xMin, d, d, false, false).render(buffer, matrices, sprite, color);
                if (b2)
                    Box.apply(xMin, range.minY(), endZ, xMax, range.minY(), endZ, xMax - xMin, d, d, false, false).render(buffer, matrices, sprite, color);
                if (b3)
                    Box.apply(startX, range.minY(), zMin, startX, range.minY(), zMax, d, d, zMax - zMin, false, false).render(buffer, matrices, sprite, color);
                if (b4)
                    Box.apply(endX, range.minY(), zMin, endX, range.minY(), zMax, d, d, zMax - zMin, false, false).render(buffer, matrices, sprite, color);
                matrices.popPose();
                Minecraft.getInstance().getProfiler().pop();
            }
            Minecraft.getInstance().getProfiler().pop();
        }

        Minecraft.getInstance().getProfiler().pop();
    }
}
