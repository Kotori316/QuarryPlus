package com.yogpc.qp.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.marker.ChunkMarkerBlock;
import com.yogpc.qp.machine.marker.ChunkMarkerEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.Vec3;

import static com.yogpc.qp.render.RenderMarker.renderLink;

public class RenderChunkMarker implements BlockEntityRenderer<ChunkMarkerEntity> {
    @SuppressWarnings("unused")
    public RenderChunkMarker(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ChunkMarkerEntity marker, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int light, int overlay, Vec3 vec3) {
        ProfilerFiller profiler = Profiler.get();
        profiler.push(QuarryPlus.modID);
        profiler.push(ChunkMarkerBlock.NAME);

        poseStack.pushPose();
        BlockPos markerPos = marker.getBlockPos();
        poseStack.translate(-markerPos.getX(), -markerPos.getY(), -markerPos.getZ());
        marker.getLink().ifPresent(link -> renderLink(poseStack, bufferSource, link, ColorBox.redColor));
        poseStack.popPose();

        profiler.pop();
        profiler.pop();
    }
}
