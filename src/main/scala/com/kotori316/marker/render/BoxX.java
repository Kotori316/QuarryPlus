package com.kotori316.marker.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.MathHelper;

class BoxX extends Box {
    public BoxX(final double startX, final double endX, final double y, final double z, final double sizeX, final double sizeY, final double sizeZ, final boolean firstSide, final boolean endSide) {
        super(startX, y, z, endX, y, z, sizeX, sizeY, sizeZ, firstSide, endSide);
    }

    @Override
    public void render(IVertexBuilder b, MatrixStack matrixStack, TextureAtlasSprite sprite, ColorBox colorBox) {
        int count = MathHelper.floor(this.length / super.sizeX);
        float minU = sprite.getMinU();
        float minV = sprite.getMinV();
        float maXV = sprite.getInterpolatedV(super.sizeX / this.maxSize * (double) 16);
        float maYU = sprite.getInterpolatedU(super.sizeY / this.maxSize * (double) 16);
        float maZU = sprite.getInterpolatedU(super.sizeZ / this.maxSize * (double) 16);
        float maZV = sprite.getInterpolatedV(super.sizeZ / this.maxSize * (double) 16);
        int red = colorBox.red;
        int green = colorBox.green;
        int blue = colorBox.blue;
        int alpha = colorBox.alpha;
        Buffer buffer = new Buffer(b, matrixStack);
        if (super.firstSide) {
            buffer.pos(super.startX, super.endY + this.offY, super.endZ - this.offZ).color(red, green, blue, alpha).tex(minU, minV).lightedAndEnd();
            buffer.pos(super.startX, super.endY - this.offY, super.endZ - this.offZ).color(red, green, blue, alpha).tex(maYU, minV).lightedAndEnd();
            buffer.pos(super.startX, super.endY - this.offY, super.endZ + this.offZ).color(red, green, blue, alpha).tex(maYU, maZV).lightedAndEnd();
            buffer.pos(super.startX, super.endY + this.offY, super.endZ + this.offZ).color(red, green, blue, alpha).tex(minU, maZV).lightedAndEnd();
        }

        for (int i1 = 0; i1 <= count; ++i1) {
            double i2 = i1 == count ? this.length / super.sizeX : (double) i1 + 1.0D;
            buffer.pos(super.startX + super.sizeX * i2, super.endY + this.offY, super.endZ - this.offZ).color(red, green, blue, alpha).tex(minU, minV).lightedAndEnd();
            buffer.pos(super.startX + super.sizeX * i2, super.endY - this.offY, super.endZ - this.offZ).color(red, green, blue, alpha).tex(maYU, minV).lightedAndEnd();
            buffer.pos(super.startX + super.sizeX * (double) i1, super.endY - this.offY, super.endZ - this.offZ).color(red, green, blue, alpha).tex(maYU, maXV).lightedAndEnd();
            buffer.pos(super.startX + super.sizeX * (double) i1, super.endY + this.offY, super.endZ - this.offZ).color(red, green, blue, alpha).tex(minU, maXV).lightedAndEnd();
            buffer.pos(super.startX + super.sizeX * (double) i1, super.endY + this.offY, super.endZ + this.offZ).color(red, green, blue, alpha).tex(minU, minV).lightedAndEnd();
            buffer.pos(super.startX + super.sizeX * (double) i1, super.endY - this.offY, super.endZ + this.offZ).color(red, green, blue, alpha).tex(maYU, minV).lightedAndEnd();
            buffer.pos(super.startX + super.sizeX * i2, super.endY - this.offY, super.endZ + this.offZ).color(red, green, blue, alpha).tex(maYU, maXV).lightedAndEnd();
            buffer.pos(super.startX + super.sizeX * i2, super.endY + this.offY, super.endZ + this.offZ).color(red, green, blue, alpha).tex(minU, maXV).lightedAndEnd();
            buffer.pos(super.startX + super.sizeX * (double) i1, super.endY + this.offY, super.endZ - this.offZ).color(red, green, blue, alpha).tex(minU, minV).lightedAndEnd();
            buffer.pos(super.startX + super.sizeX * (double) i1, super.endY + this.offY, super.endZ + this.offZ).color(red, green, blue, alpha).tex(maZU, minV).lightedAndEnd();
            buffer.pos(super.startX + super.sizeX * i2, super.endY + this.offY, super.endZ + this.offZ).color(red, green, blue, alpha).tex(maZU, maXV).lightedAndEnd();
            buffer.pos(super.startX + super.sizeX * i2, super.endY + this.offY, super.endZ - this.offZ).color(red, green, blue, alpha).tex(minU, maXV).lightedAndEnd();
            buffer.pos(super.startX + super.sizeX * i2, super.endY - this.offY, super.endZ - this.offZ).color(red, green, blue, alpha).tex(minU, minV).lightedAndEnd();
            buffer.pos(super.startX + super.sizeX * i2, super.endY - this.offY, super.endZ + this.offZ).color(red, green, blue, alpha).tex(maZU, minV).lightedAndEnd();
            buffer.pos(super.startX + super.sizeX * (double) i1, super.endY - this.offY, super.endZ + this.offZ).color(red, green, blue, alpha).tex(maZU, maXV).lightedAndEnd();
            buffer.pos(super.startX + super.sizeX * (double) i1, super.endY - this.offY, super.endZ - this.offZ).color(red, green, blue, alpha).tex(minU, maXV).lightedAndEnd();
        }

        if (super.endSide) {
            buffer.pos(super.endX, super.endY + this.offY, super.endZ + this.offZ).color(red, green, blue, alpha).tex(minU, minV).lightedAndEnd();
            buffer.pos(super.endX, super.endY - this.offY, super.endZ + this.offZ).color(red, green, blue, alpha).tex(maYU, minV).lightedAndEnd();
            buffer.pos(super.endX, super.endY - this.offY, super.endZ - this.offZ).color(red, green, blue, alpha).tex(maYU, maZV).lightedAndEnd();
            buffer.pos(super.endX, super.endY + this.offY, super.endZ - this.offZ).color(red, green, blue, alpha).tex(minU, maZV).lightedAndEnd();
        }

    }
}
