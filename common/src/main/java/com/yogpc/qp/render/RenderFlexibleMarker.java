package com.yogpc.qp.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.marker.FlexibleMarkerBlock;
import com.yogpc.qp.machine.marker.FlexibleMarkerEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import static com.yogpc.qp.render.RenderMarker.renderLink;

public class RenderFlexibleMarker implements BlockEntityRenderer<FlexibleMarkerEntity> {
    @SuppressWarnings("unused")
    public RenderFlexibleMarker(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(FlexibleMarkerEntity marker, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ProfilerFiller profiler = Profiler.get();
        profiler.push(QuarryPlus.modID);
        profiler.push(FlexibleMarkerBlock.NAME);

        poseStack.pushPose();
        BlockPos markerPos = marker.getBlockPos();
        poseStack.translate(-markerPos.getX(), -markerPos.getY(), -markerPos.getZ());

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
            directionBox.render(bufferSource.getBuffer(RenderType.cutout()), poseStack, Sprites.INSTANCE.getWhite(), ColorBox.blueColor);
        }

        marker.getLink().ifPresent(link -> renderLink(poseStack, bufferSource, link, ColorBox.redColor));
        poseStack.popPose();

        profiler.pop();
        profiler.pop();
    }
}
