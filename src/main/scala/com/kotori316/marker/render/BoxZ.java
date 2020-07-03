package com.kotori316.marker.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.MathHelper;

class BoxZ extends Box {
    final double length;

    public BoxZ(final double startZ, final double endZ, final double x, final double y, final double sizeX, final double sizeY, final double sizeZ, final boolean firstSide, final boolean endSide) {
        super(x, y, startZ, x, y, endZ, sizeX, sizeY, sizeZ, firstSide, endSide);
        this.length = dz;
    }

    @Override
    public void render(final IVertexBuilder b, MatrixStack matrixStack, final TextureAtlasSprite sprite, final ColorBox colorBox) {
        int count = MathHelper.floor(this.length / super.sizeZ);
        float minU = sprite.getMinU();
        float minV = sprite.getMinV();
        float maXU = sprite.getInterpolatedU(super.sizeX / this.maxSize * (double) 16);
        float maXV = sprite.getInterpolatedV(super.sizeX / this.maxSize * (double) 16);
        float maYU = sprite.getInterpolatedU(super.sizeY / this.maxSize * (double) 16);
        float maZV = sprite.getInterpolatedV(super.sizeZ / this.maxSize * (double) 16);
        int red = colorBox.red;
        int green = colorBox.green;
        int blue = colorBox.blue;
        int alpha = colorBox.alpha;
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
