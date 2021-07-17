package com.yogpc.qp.render;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;

class BoxXZ extends Box {
    final double length;

    public BoxXZ(final double startX, final double startZ, final double endX, final double y, final double endZ, final double sizeX, final double sizeY, final double sizeZ, final boolean firstSide, final boolean endSide) {
        super(startX, y, startZ, endX, y, endZ, sizeX, sizeY, sizeZ, firstSide, endSide);
        length = Math.sqrt(this.dx * this.dx + this.dz * this.dz);
    }

    @Override
    public void render(final VertexConsumer buffer, MatrixStack matrixStack, final Sprite sprite, final ColorBox colorBox) {
        double n2Size = this.length;
        this.renderInternal(buffer, matrixStack, sprite, 0.0D, 0.5D, 0.0D, -this.dz / n2Size / (double) 2, this.dx / n2Size / (double) 2, colorBox.alpha(), colorBox.red(), colorBox.green(), colorBox.blue());
    }

}
