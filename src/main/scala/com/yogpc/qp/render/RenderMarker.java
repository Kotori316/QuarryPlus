package com.yogpc.qp.render;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

import com.mojang.blaze3d.vertex.PoseStack;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.Area;
import com.yogpc.qp.machines.marker.TileMarker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderMarker implements BlockEntityRenderer<TileMarker> {
    public static BlockEntityRendererProvider<TileMarker> constructor(boolean reduceLineIfPlayerIsFar) {
        return c -> new RenderMarker(c, reduceLineIfPlayerIsFar);
    }

    @SuppressWarnings("unused")
    public RenderMarker(BlockEntityRendererProvider.Context context, boolean reduceLineIfPlayerIsFar) {
        this.reduceLineIfPlayerIsFar = reduceLineIfPlayerIsFar;
    }

    private static final double a = 0.5d, b = 10d / 16d, c = 6d / 16d;
    final boolean reduceLineIfPlayerIsFar;

    @Override
    public void render(TileMarker entity, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
        Box[] renderBoxes;
        TextureAtlasSprite sprite;
        ColorBox color;
        var optionalBoxes = entity.renderArea();
        var markerPos = entity.getBlockPos();
        if (optionalBoxes.isPresent()) {
            renderBoxes = optionalBoxes.get();
            sprite = Sprites.INSTANCE.getWhite();
            color = ColorBox.markerBlueColor;
        } else if (entity.rsReceiving && entity.getArea().isEmpty()) {
            var player = Minecraft.getInstance().player;
            var playerX = player == null ? markerPos.getX() : player.getX();
            var playerZ = player == null ? markerPos.getZ() : player.getZ();
            int visibleRange = 32;
            var xMin = Math.max(playerX - visibleRange, markerPos.getX() - TileMarker.MAX_SEARCH);
            var xMax = Math.min(playerX + visibleRange, markerPos.getX() + TileMarker.MAX_SEARCH);
            var zMin = Math.max(playerZ - visibleRange, markerPos.getZ() - TileMarker.MAX_SEARCH);
            var zMax = Math.min(playerZ + visibleRange, markerPos.getZ() + TileMarker.MAX_SEARCH);
            renderBoxes = Stream.of(
                    !reduceLineIfPlayerIsFar || Math.abs(markerPos.getZ() - playerZ) < visibleRange ? Box.apply(xMin + b, markerPos.getY() + a, markerPos.getZ() + a,
                        xMax + c, markerPos.getY() + a, markerPos.getZ() + a,
                        xMax - xMin - 0.25d, 1d / 8d + 0.001d, 1d / 8d + 0.001d, false, false) : null,
                    !reduceLineIfPlayerIsFar || Math.abs(markerPos.getX() - playerX) < visibleRange ? Box.apply(markerPos.getX() + a, markerPos.getY() + a, zMin + b,
                        markerPos.getX() + a, markerPos.getY() + a, zMax + c,
                        1d / 8d + 0.001d, 1d / 8d + 0.001d, zMax - zMin - 0.25d, false, false) : null
                ).filter(Objects::nonNull)
                .toArray(Box[]::new);
            sprite = Sprites.INSTANCE.getWhite();
            color = ColorBox.markerRedColor;
        } else {
            return;
        }
        Minecraft.getInstance().getProfiler().push(QuarryPlus.modID);
        Minecraft.getInstance().getProfiler().push("RenderMarker");
        var buffer = vertexConsumers.getBuffer(RenderType.cutout());
        matrices.pushPose();
        matrices.translate(-markerPos.getX(), -markerPos.getY(), -markerPos.getZ());
        for (Box box : renderBoxes) {
            box.render(buffer, matrices, sprite, color);
        }
        matrices.popPose();

        Minecraft.getInstance().getProfiler().pop();
        Minecraft.getInstance().getProfiler().pop();
    }

    public static Box[] getRenderBox(Area area) {
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
        AABB[] lineBoxes = new AABB[12];
        if ((flag & 1) == 1) {//x
            lineBoxes[0] = new AABB(xMin + b, yMin + a, zMin + a, xMax + c, yMin + a, zMin + a);
        }
        if ((flag & 2) == 2) {//y
            lineBoxes[4] = new AABB(xMin + a, yMin + b, zMin + a, xMin + a, yMax + c, zMin + a);
        }
        if ((flag & 4) == 4) {//z
            lineBoxes[8] = new AABB(xMin + a, yMin + a, zMin + b, xMin + a, yMin + a, zMax + c);
        }
        if ((flag & 3) == 3) {//xy
            lineBoxes[2] = new AABB(xMin + b, yMax + a, zMin + a, xMax + c, yMax + a, zMin + a);
            lineBoxes[6] = new AABB(xMax + a, yMin + b, zMin + a, xMax + a, yMax + c, zMin + a);
        }
        if ((flag & 5) == 5) {//xz
            lineBoxes[1] = new AABB(xMin + b, yMin + a, zMax + a, xMax + c, yMin + a, zMax + a);
            lineBoxes[9] = new AABB(xMax + a, yMin + a, zMin + b, xMax + a, yMin + a, zMax + c);
        }
        if ((flag & 6) == 6) {//yz
            lineBoxes[5] = new AABB(xMin + a, yMin + b, zMax + a, xMin + a, yMax + c, zMax + a);
            lineBoxes[10] = new AABB(xMin + a, yMax + a, zMin + b, xMin + a, yMax + a, zMax + c);
        }
        if ((flag & 7) == 7) {//xyz
            lineBoxes[3] = new AABB(xMin + b, yMax + a, zMax + a, xMax + c, yMax + a, zMax + a);
            lineBoxes[7] = new AABB(xMax + a, yMin + b, zMax + a, xMax + a, yMax + c, zMax + a);
            lineBoxes[11] = new AABB(xMax + a, yMax + a, zMin + b, xMax + a, yMax + a, zMax + c);
        }

        return Arrays.stream(lineBoxes).filter(Objects::nonNull)
            .map(range -> Box.apply(range,
                Math.max(range.getXsize(), 1d / 8d + 0.001d),
                Math.max(range.getYsize(), 1d / 8d + 0.001d),
                Math.max(range.getZsize(), 1d / 8d + 0.001d),
                false, false))
            .toArray(Box[]::new);
    }

    @Override
    public int getViewDistance() {
        return TileMarker.MAX_SEARCH;
    }

    @Override
    public boolean shouldRenderOffScreen(TileMarker blockEntity) {
        return true;
    }
}
