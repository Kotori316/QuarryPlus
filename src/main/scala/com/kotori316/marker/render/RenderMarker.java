package com.kotori316.marker.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.BlockPos;

import com.kotori316.marker.TileFlexMarker;

public class RenderMarker extends TileEntityRenderer<TileFlexMarker> {

    public RenderMarker(TileEntityRendererDispatcher d) {
        super(d);
    }

    @Override
    public void render(TileFlexMarker te, float partialTicks, MatrixStack matrix, IRenderTypeBuffer renderTypeBuffer, int otherLight, int light) {
        Minecraft.getInstance().getProfiler().startSection("marker");
        BlockPos pos = te.getPos();
        IVertexBuilder buffer = renderTypeBuffer.getBuffer(RenderType.getCutout());
        matrix.push();
        matrix.translate(-pos.getX(), -pos.getY(), -pos.getZ());
        if (te.boxes != null) {
            for (Box box : te.boxes) {
                box.render(buffer, matrix, Resources.getInstance().spriteWhite, ColorBox.redColor);
            }
        }
        if (te.directionBox != null) {
            te.directionBox.render(buffer, matrix, Resources.getInstance().spriteWhite, ColorBox.blueColor);
        }
        matrix.pop();
        Minecraft.getInstance().getProfiler().endSection();
    }

    @Override
    public boolean isGlobalRenderer(TileFlexMarker te) {
        return true;
    }
}
