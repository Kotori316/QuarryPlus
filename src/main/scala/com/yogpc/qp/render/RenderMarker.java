package com.yogpc.qp.render;

import java.util.Arrays;
import java.util.Objects;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.Area;
import com.yogpc.qp.machines.marker.TileMarker;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;

@Environment(EnvType.CLIENT)
public class RenderMarker implements BlockEntityRenderer<TileMarker> {
    @SuppressWarnings("unused")
    public RenderMarker(BlockEntityRendererFactory.Context context) {
    }

    @Override
    public void render(TileMarker entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        var areaOptional = entity.renderArea();
        if (areaOptional.isEmpty()) return;
        MinecraftClient.getInstance().getProfiler().push(QuarryPlus.modID);
        MinecraftClient.getInstance().getProfiler().push("RenderMarker");
        var area = areaOptional.get();
        var buffer = vertexConsumers.getBuffer(RenderLayer.getCutoutMipped());
        matrices.push();
        var markerPos = entity.getPos();
        matrices.translate(-markerPos.getX(), -markerPos.getY(), -markerPos.getZ());
        for (Box box : getRenderBox(area)) {
            box.render(buffer, matrices, Sprites.INSTANCE.getMarkerBlue(), ColorBox.white);
        }
        matrices.pop();

        MinecraftClient.getInstance().getProfiler().pop();
        MinecraftClient.getInstance().getProfiler().pop();
    }

    static Box[] getRenderBox(Area area) {
        final double a = 0.5d, b = 10d / 16d, c = 6d / 16d;
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
        net.minecraft.util.math.Box[] lineBoxes = new net.minecraft.util.math.Box[12];
        if ((flag & 1) == 1) {//x
            lineBoxes[0] = new net.minecraft.util.math.Box(xMin + b, yMin + a, zMin + a, xMax + c, yMin + a, zMin + a);
        }
        if ((flag & 2) == 2) {//y
            lineBoxes[4] = new net.minecraft.util.math.Box(xMin + a, yMin + b, zMin + a, xMin + a, yMax + c, zMin + a);
        }
        if ((flag & 4) == 4) {//z
            lineBoxes[8] = new net.minecraft.util.math.Box(xMin + a, yMin + a, zMin + b, xMin + a, yMin + a, zMax + c);
        }
        if ((flag & 3) == 3) {//xy
            lineBoxes[2] = new net.minecraft.util.math.Box(xMin + b, yMax + a, zMin + a, xMax + c, yMax + a, zMin + a);
            lineBoxes[6] = new net.minecraft.util.math.Box(xMax + a, yMin + b, zMin + a, xMax + a, yMax + c, zMin + a);
        }
        if ((flag & 5) == 5) {//xz
            lineBoxes[1] = new net.minecraft.util.math.Box(xMin + b, yMin + a, zMax + a, xMax + c, yMin + a, zMax + a);
            lineBoxes[9] = new net.minecraft.util.math.Box(xMax + a, yMin + a, zMin + b, xMax + a, yMin + a, zMax + c);
        }
        if ((flag & 6) == 6) {//yz
            lineBoxes[5] = new net.minecraft.util.math.Box(xMin + a, yMin + b, zMax + a, xMin + a, yMax + c, zMax + a);
            lineBoxes[10] = new net.minecraft.util.math.Box(xMin + a, yMax + a, zMin + b, xMin + a, yMax + a, zMax + c);
        }
        if ((flag & 7) == 7) {//xyz
            lineBoxes[3] = new net.minecraft.util.math.Box(xMin + b, yMax + a, zMax + a, xMax + c, yMax + a, zMax + a);
            lineBoxes[7] = new net.minecraft.util.math.Box(xMax + a, yMin + b, zMax + a, xMax + a, yMax + c, zMax + a);
            lineBoxes[11] = new net.minecraft.util.math.Box(xMax + a, yMax + a, zMin + b, xMax + a, yMax + a, zMax + c);
        }

        return Arrays.stream(lineBoxes).filter(Objects::nonNull)
            .map(range -> Box.apply(range, 1d / 8d + 0.001d, 1d / 8d + 0.001d, 1d / 8d + 0.001d, false, false))
            .toArray(Box[]::new);
    }
}
