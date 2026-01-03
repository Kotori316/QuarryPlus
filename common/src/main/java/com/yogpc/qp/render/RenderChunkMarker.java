package com.yogpc.qp.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.marker.ChunkMarkerBlock;
import com.yogpc.qp.machine.marker.ChunkMarkerEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import static com.yogpc.qp.render.RenderMarker.renderLink;

public class RenderChunkMarker implements BlockEntityRenderer<ChunkMarkerEntity, RenderChunkMarker.RenderChunkMarkerState> {
    @SuppressWarnings("unused")
    public RenderChunkMarker(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public RenderChunkMarkerState createRenderState() {
        return new RenderChunkMarkerState();
    }

    @Override
    public void extractRenderState(ChunkMarkerEntity blockEntity, RenderChunkMarkerState renderState, float partialTick, Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, cameraPosition, breakProgress);
        renderState.extract(blockEntity);
    }

    @Override
    public void submit(RenderChunkMarkerState renderState, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
        ProfilerFiller profiler = Profiler.get();
        profiler.push(QuarryPlus.modID);
        profiler.push(ChunkMarkerBlock.NAME);
        var marker = renderState.marker;

        poseStack.pushPose();
        BlockPos markerPos = marker.getBlockPos();
        poseStack.translate(-markerPos.getX(), -markerPos.getY(), -markerPos.getZ());
        nodeCollector.submitCustomGeometry(poseStack, Sprites.cutout(), (pose, vertexConsumer) -> {
            marker.getLink().ifPresent(link -> renderLink(pose, vertexConsumer, link, ColorBox.redColor));
        });
        poseStack.popPose();

        profiler.pop();
        profiler.pop();
    }

    public static class RenderChunkMarkerState extends BlockEntityRenderState {
        private ChunkMarkerEntity marker;

        void extract(ChunkMarkerEntity marker) {
            this.marker = marker;
        }
    }
}
