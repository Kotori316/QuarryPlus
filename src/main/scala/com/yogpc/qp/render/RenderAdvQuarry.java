package com.yogpc.qp.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.yogpc.qp.machines.advquarry.AdvQuarryAction;
import com.yogpc.qp.machines.advquarry.TileAdvQuarry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
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
    public void render(TileAdvQuarry quarry, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
        Minecraft.getInstance().getProfiler().push("quarryplus");

        if (quarry.getAction() instanceof AdvQuarryAction.MakeFrame || (quarry.getAction() == AdvQuarryAction.Waiting.WAITING)) {
            Minecraft.getInstance().getProfiler().push("chunkdestroyer");
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
                var b1 = Math.abs(playerZ - range.minZ() - 0.5) < 256;
                var b2 = Math.abs(playerZ - range.maxZ() + 1.5) < 256;
                var b3 = Math.abs(playerX - range.minX() - 0.5) < 256;
                var b4 = Math.abs(playerX - range.maxX() + 1.5) < 256;
                var xMin = Math.max(range.minX() - 0.5, playerX - 128);
                var xMax = Math.min(range.maxX() + 1.5, playerX + 128);
                var zMin = Math.max(range.minZ() - 0.5, playerZ - 128);
                var zMax = Math.min(range.maxZ() + 1.5, playerZ + 128);
                if (b1)
                    Box.apply(xMin, range.minY(), range.minZ() - 0.5, xMax, range.maxY(), range.minZ() - 0.5, d, d, d, false, false).render(buffer, matrices, sprite, color);
                if (b2)
                    Box.apply(xMin, range.minY(), range.maxZ() + 1.5, xMax, range.maxY(), range.maxZ() + 1.5, d, d, d, false, false).render(buffer, matrices, sprite, color);
                if (b3)
                    Box.apply(range.minX() - 0.5, range.minY(), zMin, range.minX() - 0.5, range.maxY(), zMax, d, d, d, false, false).render(buffer, matrices, sprite, color);
                if (b4)
                    Box.apply(range.maxX() + 1.5, range.minY(), zMin, range.maxX() + 1.5, range.maxY(), zMax, d, d, d, false, false).render(buffer, matrices, sprite, color);
                matrices.popPose();
            }
            Minecraft.getInstance().getProfiler().pop();
        }

        Minecraft.getInstance().getProfiler().pop();
    }
}
