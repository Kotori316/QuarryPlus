package com.yogpc.qp.render;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

class BoxZ extends Box {

    BoxZ(final double startZ, final double endZ, final double x, final double y, final double sizeX, final double sizeY, final double sizeZ, final boolean firstSide, final boolean endSide) {
        super(x, y, startZ, x, y, endZ, sizeX, sizeY, sizeZ, firstSide, endSide);
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    public void render(final VertexConsumer b, MatrixStack matrixStack, final Sprite sprite, final ColorBox colorBox) {
        int count = MathHelper.floor(this.length / super.sizeZ);
        float minU = sprite.getMinU();
        float minV = sprite.getMinV();
        float maXU = sprite.getFrameU(super.sizeX / this.maxSize * (double) 16);
        float maXV = sprite.getFrameV(super.sizeX / this.maxSize * (double) 16);
        float maYU = sprite.getFrameU(super.sizeY / this.maxSize * (double) 16);
        float maZV = sprite.getFrameV(super.sizeZ / this.maxSize * (double) 16);
        int red = colorBox.red();
        int green = colorBox.green();
        int blue = colorBox.blue();
        int alpha = colorBox.alpha();
        Buffer buffer = new Buffer(b, matrixStack);
        if (super.firstSide) {
            buffer.pos(super.endX + this.offX, super.endY + this.offY, super.startZ).color(red, green, blue, alpha).tex(minU, minV).lightedAndEnd();
            buffer.pos(super.endX + this.offX, super.endY - this.offY, super.startZ).color(red, green, blue, alpha).tex(maYU, minV).lightedAndEnd();
            buffer.pos(super.endX - this.offX, super.endY - this.offY, super.startZ).color(red, green, blue, alpha).tex(maYU, maXV).lightedAndEnd();
            buffer.pos(super.endX - this.offX, super.endY + this.offY, super.startZ).color(red, green, blue, alpha).tex(minU, maXV).lightedAndEnd();
        }

        for (int i1 = 0; i1 <= count; ++i1) {
            double i2 = i1 == count ? this.length / super.sizeZ : (double) i1 + 1.0D;
            buffer.pos(super.endX - this.offX, super.endY + this.offY, super.startZ + super.sizeZ * (double) i1).color(red, green, blue, alpha).tex(minU, minV).lightedAndEnd();
            buffer.pos(super.endX - this.offX, super.endY - this.offY, super.startZ + super.sizeZ * (double) i1).color(red, green, blue, alpha).tex(maYU, minV).lightedAndEnd();
            buffer.pos(super.endX - this.offX, super.endY - this.offY, super.startZ + super.sizeZ * i2).color(red, green, blue, alpha).tex(maYU, maZV).lightedAndEnd();
            buffer.pos(super.endX - this.offX, super.endY + this.offY, super.startZ + super.sizeZ * i2).color(red, green, blue, alpha).tex(minU, maZV).lightedAndEnd();
            buffer.pos(super.endX + this.offX, super.endY + this.offY, super.startZ + super.sizeZ * i2).color(red, green, blue, alpha).tex(minU, minV).lightedAndEnd();
            buffer.pos(super.endX + this.offX, super.endY - this.offY, super.startZ + super.sizeZ * i2).color(red, green, blue, alpha).tex(maYU, minV).lightedAndEnd();
            buffer.pos(super.endX + this.offX, super.endY - this.offY, super.startZ + super.sizeZ * (double) i1).color(red, green, blue, alpha).tex(maYU, maZV).lightedAndEnd();
            buffer.pos(super.endX + this.offX, super.endY + this.offY, super.startZ + super.sizeZ * (double) i1).color(red, green, blue, alpha).tex(minU, maZV).lightedAndEnd();
            buffer.pos(super.endX - this.offX, super.endY + this.offY, super.startZ + super.sizeZ * (double) i1).color(red, green, blue, alpha).tex(minU, minV).lightedAndEnd();
            buffer.pos(super.endX - this.offX, super.endY + this.offY, super.startZ + super.sizeZ * i2).color(red, green, blue, alpha).tex(maXU, minV).lightedAndEnd();
            buffer.pos(super.endX + this.offX, super.endY + this.offY, super.startZ + super.sizeZ * i2).color(red, green, blue, alpha).tex(maXU, maZV).lightedAndEnd();
            buffer.pos(super.endX + this.offX, super.endY + this.offY, super.startZ + super.sizeZ * (double) i1).color(red, green, blue, alpha).tex(minU, maZV).lightedAndEnd();
            buffer.pos(super.endX + this.offX, super.endY - this.offY, super.startZ + super.sizeZ * (double) i1).color(red, green, blue, alpha).tex(minU, minV).lightedAndEnd();
            buffer.pos(super.endX + this.offX, super.endY - this.offY, super.startZ + super.sizeZ * i2).color(red, green, blue, alpha).tex(maXU, minV).lightedAndEnd();
            buffer.pos(super.endX - this.offX, super.endY - this.offY, super.startZ + super.sizeZ * i2).color(red, green, blue, alpha).tex(maXU, maZV).lightedAndEnd();
            buffer.pos(super.endX - this.offX, super.endY - this.offY, super.startZ + super.sizeZ * (double) i1).color(red, green, blue, alpha).tex(minU, maZV).lightedAndEnd();
        }

        if (super.endSide) {
            buffer.pos(super.endX - this.offX, super.endY + this.offY, super.endZ).color(red, green, blue, alpha).tex(minU, minV).lightedAndEnd();
            buffer.pos(super.endX - this.offX, super.endY - this.offY, super.endZ).color(red, green, blue, alpha).tex(maYU, minV).lightedAndEnd();
            buffer.pos(super.endX + this.offX, super.endY - this.offY, super.endZ).color(red, green, blue, alpha).tex(maYU, maXV).lightedAndEnd();
            buffer.pos(super.endX + this.offX, super.endY + this.offY, super.endZ).color(red, green, blue, alpha).tex(minU, maXV).lightedAndEnd();
        }

    }
}
