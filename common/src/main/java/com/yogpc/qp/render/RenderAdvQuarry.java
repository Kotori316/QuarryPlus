package com.yogpc.qp.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.Area;
import com.yogpc.qp.machine.advquarry.AdvQuarryBlock;
import com.yogpc.qp.machine.advquarry.AdvQuarryEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class RenderAdvQuarry implements BlockEntityRenderer<AdvQuarryEntity, RenderAdvQuarry.RenderAdvQuarryState> {
    @SuppressWarnings("unused")
    public RenderAdvQuarry(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public RenderAdvQuarryState createRenderState() {
        return new RenderAdvQuarryState();
    }

    @Override
    public void extractRenderState(AdvQuarryEntity blockEntity, RenderAdvQuarryState renderState, float partialTick, Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, cameraPosition, breakProgress);
        renderState.extract(blockEntity);
    }

    @Override
    public void submit(RenderAdvQuarryState renderState, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
        ProfilerFiller profiler = Profiler.get();
        profiler.push(QuarryPlus.modID);

        var quarry = renderState.quarry;
        if (quarry.renderMode().equals("frame")) {
            profiler.push(AdvQuarryBlock.NAME);
            Area range = quarry.getArea();
            if (range != null) {
                profiler.push("rendering");
                final double d = 1d / 16d;
                final TextureAtlasSprite sprite = Sprites.INSTANCE.getWhite();
                final ColorBox color = new ColorBox(0xFF, 0xFF, 0, 0xFF);
                BlockPos pos = quarry.getBlockPos();
                LocalPlayer player = Minecraft.getInstance().player;
                double playerX = player == null ? pos.getX() : player.getX(); //x
                double playerZ = player == null ? pos.getZ() : player.getZ(); //z
                poseStack.pushPose();
                poseStack.translate(-pos.getX(), -pos.getY(), -pos.getZ()); // Offset
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
                nodeCollector.submitCustomGeometry(poseStack, Sprites.cutout(), (pose, vertexConsumer) -> {
                    if (b1)
                        Box.apply(xMin, range.minY(), startZ, xMax, range.minY(), startZ, xMax - xMin, d, d, false, false).render(vertexConsumer, pose, sprite, color);
                    if (b2)
                        Box.apply(xMin, range.minY(), endZ, xMax, range.minY(), endZ, xMax - xMin, d, d, false, false).render(vertexConsumer, pose, sprite, color);
                    if (b3)
                        Box.apply(startX, range.minY(), zMin, startX, range.minY(), zMax, d, d, zMax - zMin, false, false).render(vertexConsumer, pose, sprite, color);
                    if (b4)
                        Box.apply(endX, range.minY(), zMin, endX, range.minY(), zMax, d, d, zMax - zMin, false, false).render(vertexConsumer, pose, sprite, color);
                });
                poseStack.popPose();
                profiler.pop();
            }
            profiler.pop();
        }

        profiler.pop();
    }

    public static class RenderAdvQuarryState extends BlockEntityRenderState {
        AdvQuarryEntity quarry;

        void extract(AdvQuarryEntity quarry) {
            this.quarry = quarry;
        }
    }
}
