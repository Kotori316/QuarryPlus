package com.yogpc.qp.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.advquarry.BlockAdvQuarry;
import com.yogpc.qp.machines.advquarry.TileAdvQuarry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

@Environment(EnvType.CLIENT)
public class RenderAdvQuarry implements BlockEntityRenderer<TileAdvQuarry> {
    @SuppressWarnings("unused")
    public RenderAdvQuarry(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen(TileAdvQuarry blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    @SuppressWarnings("DuplicatedCode") // for readability.
    public void render(TileAdvQuarry quarry, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
        Minecraft.getInstance().getProfiler().push(QuarryPlus.modID);

        if ("MakeFrame".equals(quarry.actionKey) || "Waiting".equals(quarry.actionKey)) {
            Minecraft.getInstance().getProfiler().push(BlockAdvQuarry.NAME);
            var range = quarry.getArea();
            if (range != null) {
                final double d = 1d / 16d;
                final TextureAtlasSprite sprite = Sprites.INSTANCE.getWhite();
                final ColorBox color = new ColorBox(0xFF, 0xFF, 0, 0xFF);
                var buffer = vertexConsumers.getBuffer(RenderType.cutout());
                var pos = quarry.getBlockPos();
                var player = Minecraft.getInstance().player;
                var playerX = player == null ? pos.getX() : player.getX(); //x
                var playerZ = player == null ? pos.getZ() : player.getZ(); //z
                matrices.pushPose();
                matrices.translate(-pos.getX(), -pos.getY(), -pos.getZ()); // Offset
                var startX = range.minX() + 0.5;
                var startZ = range.minZ() + 0.5;
                var endZ = range.maxZ() + 0.5;
                var endX = range.maxX() + 0.5;
                var b1 = Math.abs(playerZ - startZ) < 256;
                var b2 = Math.abs(playerZ - endZ) < 256;
                var b3 = Math.abs(playerX - startX) < 256;
                var b4 = Math.abs(playerX - endX) < 256;
                var xMin = Math.max(startX, playerX - 128);
                var xMax = Math.min(endX, playerX + 128);
                var zMin = Math.max(startZ, playerZ - 128);
                var zMax = Math.min(endZ, playerZ + 128);
                if (b1)
                    Box.apply(xMin, range.minY(), startZ, xMax, range.minY(), startZ, xMax - xMin, d, d, false, false).render(buffer, matrices, sprite, color);
                if (b2)
                    Box.apply(xMin, range.minY(), endZ, xMax, range.minY(), endZ, xMax - xMin, d, d, false, false).render(buffer, matrices, sprite, color);
                if (b3)
                    Box.apply(startX, range.minY(), zMin, startX, range.minY(), zMax, d, d, zMax - zMin, false, false).render(buffer, matrices, sprite, color);
                if (b4)
                    Box.apply(endX, range.minY(), zMin, endX, range.minY(), zMax, d, d, zMax - zMin, false, false).render(buffer, matrices, sprite, color);
                matrices.popPose();
            }
            Minecraft.getInstance().getProfiler().pop();
        }

        Minecraft.getInstance().getProfiler().pop();
    }
}
