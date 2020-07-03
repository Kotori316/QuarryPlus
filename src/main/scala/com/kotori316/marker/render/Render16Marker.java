package com.kotori316.marker.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.math.BlockPos;

import com.kotori316.marker.Tile16Marker;

public class Render16Marker extends TileEntityRenderer<Tile16Marker> {
    public Render16Marker(TileEntityRendererDispatcher d) {
        super(d);
    }

    @Override
    public void render(Tile16Marker te, float partialTicks, MatrixStack matrix, IRenderTypeBuffer renderTypeBuffer, int otherLight, int light) {
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
        matrix.pop();
        Minecraft.getInstance().getProfiler().endSection();
    }

    @Override
    public boolean isGlobalRenderer(Tile16Marker te) {
        return true;
    }
}
