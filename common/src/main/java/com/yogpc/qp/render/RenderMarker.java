package com.yogpc.qp.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.Area;
import com.yogpc.qp.machine.marker.NormalMarkerEntity;
import com.yogpc.qp.machine.marker.QuarryMarker;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

public class RenderMarker implements BlockEntityRenderer<NormalMarkerEntity> {
    @SuppressWarnings("unused")
    public RenderMarker(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(NormalMarkerEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ProfilerFiller profiler = Profiler.get();
        profiler.push(QuarryPlus.modID);
        profiler.push("RenderMarker");

        if (blockEntity.getStatus() == NormalMarkerEntity.Status.CONNECTED_MASTER) {
            var markerPos = blockEntity.getBlockPos();
            poseStack.pushPose();
            poseStack.translate(-markerPos.getX(), -markerPos.getY(), -markerPos.getZ());
            blockEntity.getLink().ifPresent(link -> renderLink(poseStack, bufferSource, link, ColorBox.markerBlueColor));
            poseStack.popPose();
        }

        profiler.pop();
        profiler.pop();
    }

    public static void renderLink(PoseStack poseStack, MultiBufferSource bufferSource, QuarryMarker.Link link, ColorBox color) {
        var sprite = Sprites.INSTANCE.getWhite();
        var buffer = bufferSource.getBuffer(RenderType.cutout());
        for (Box box : getRenderBox(link.area())) {
            box.render(buffer, poseStack, sprite, color);
        }
    }

    private static final double a = 0.5d, b = 10d / 16d, c = 6d / 16d;

    public static List<Box> getRenderBox(Area area) {
        int flag = 0;
        int xMin = area.minX();
        int yMin = area.minY();
        int zMin = area.minZ();
        int xMax = area.maxX();
        int yMax = area.maxY();
        int zMax = area.maxZ();
        if (xMin != xMax)
            flag |= 1;
        if (yMin != yMax)
            flag |= 2;
        if (zMin != zMax)
            flag |= 4;
        List<AABB> boxes = new ArrayList<>(12);
        if ((flag & 1) == 1) {//x
            boxes.add(new AABB(xMin + b, yMin + a, zMin + a, xMax + c, yMin + a, zMin + a));
        }
        if ((flag & 2) == 2) {//y
            boxes.add(new AABB(xMin + a, yMin + b, zMin + a, xMin + a, yMax + c, zMin + a));
        }
        if ((flag & 4) == 4) {//z
            boxes.add(new AABB(xMin + a, yMin + a, zMin + b, xMin + a, yMin + a, zMax + c));
        }
        if ((flag & 3) == 3) {//xy
            boxes.add(new AABB(xMin + b, yMax + a, zMin + a, xMax + c, yMax + a, zMin + a));
            boxes.add(new AABB(xMax + a, yMin + b, zMin + a, xMax + a, yMax + c, zMin + a));
        }
        if ((flag & 5) == 5) {//xz
            boxes.add(new AABB(xMin + b, yMin + a, zMax + a, xMax + c, yMin + a, zMax + a));
            boxes.add(new AABB(xMax + a, yMin + a, zMin + b, xMax + a, yMin + a, zMax + c));
        }
        if ((flag & 6) == 6) {//yz
            boxes.add(new AABB(xMin + a, yMin + b, zMax + a, xMin + a, yMax + c, zMax + a));
            boxes.add(new AABB(xMin + a, yMax + a, zMin + b, xMin + a, yMax + a, zMax + c));
        }
        if ((flag & 7) == 7) {//xyz
            boxes.add(new AABB(xMin + b, yMax + a, zMax + a, xMax + c, yMax + a, zMax + a));
            boxes.add(new AABB(xMax + a, yMin + b, zMax + a, xMax + a, yMax + c, zMax + a));
            boxes.add(new AABB(xMax + a, yMax + a, zMin + b, xMax + a, yMax + a, zMax + c));
        }

        return boxes.stream()
            .map(range ->
                Box.apply(range,
                    Math.max(range.getXsize(), 1d / 8d + 0.001d),
                    Math.max(range.getYsize(), 1d / 8d + 0.001d),
                    Math.max(range.getZsize(), 1d / 8d + 0.001d),
                    true, true))
            .toList();
    }
}
