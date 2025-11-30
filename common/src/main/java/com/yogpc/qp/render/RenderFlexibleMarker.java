package com.yogpc.qp.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.marker.FlexibleMarkerBlock;
import com.yogpc.qp.machine.marker.FlexibleMarkerEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import static com.yogpc.qp.render.RenderMarker.renderLink;

public class RenderFlexibleMarker implements BlockEntityRenderer<FlexibleMarkerEntity, RenderFlexibleMarker.RenderFlexibleMarkerState> {
    @SuppressWarnings("unused")
    public RenderFlexibleMarker(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public RenderFlexibleMarkerState createRenderState() {
        return new RenderFlexibleMarkerState();
    }

    @Override
    public void extractRenderState(FlexibleMarkerEntity blockEntity, RenderFlexibleMarkerState renderState, float partialTick, Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, cameraPosition, breakProgress);
        renderState.extract(blockEntity);
    }

    @Override
    public void submit(RenderFlexibleMarkerState renderState, PoseStack poseStack, SubmitNodeCollector nodeCollector, CameraRenderState cameraRenderState) {
        ProfilerFiller profiler = Profiler.get();
        profiler.push(QuarryPlus.modID);
        profiler.push(FlexibleMarkerBlock.NAME);
        var marker = renderState.marker;

        poseStack.pushPose();
        BlockPos markerPos = marker.getBlockPos();
        poseStack.translate(-markerPos.getX(), -markerPos.getY(), -markerPos.getZ());

        nodeCollector.submitCustomGeometry(poseStack, RenderType.cutout(), (pose, vertexConsumer) -> {
            var direction = marker.getDirection();
            AABB bb;
            final double a = 0.5d, c = 6d / 16d;
            if (direction != null) {
                if (direction.getAxis() == Direction.Axis.X) {
                    bb = new AABB(markerPos.getX() - c + a, markerPos.getY() + a, markerPos.getZ() + a,
                        markerPos.getX() + c + a, markerPos.getY() + a, markerPos.getZ() + a);
                } else {
                    bb = new AABB(markerPos.getX() + a, markerPos.getY() + a, markerPos.getZ() - c + a,
                        markerPos.getX() + a, markerPos.getY() + a, markerPos.getZ() + c + a);
                }
                var directionBox = Box.apply(bb.move(Vec3.atLowerCornerOf(direction.getUnitVec3i()).scale(a)), 1d / 8d, 1d / 8d, 1d / 8d, true, true);
                directionBox.render(vertexConsumer, pose, Sprites.INSTANCE.getWhite(), ColorBox.blueColor);
            }

            marker.getLink().ifPresent(link -> renderLink(pose, vertexConsumer, link, ColorBox.redColor));
        });
        poseStack.popPose();

        profiler.pop();
        profiler.pop();
    }

    public static class RenderFlexibleMarkerState extends BlockEntityRenderState {
        private FlexibleMarkerEntity marker;

        void extract(FlexibleMarkerEntity marker) {
            this.marker = marker;
        }
    }
}
