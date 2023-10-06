package com.yogpc.qp.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;

class BoxY extends Box {

    BoxY(final double startY, final double endY, final double x, final double z, final double sizeX, final double sizeY, final double sizeZ, final boolean firstSide, final boolean endSide) {
        super(x, startY, z, x, endY, z, sizeX, sizeY, sizeZ, firstSide, endSide);
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    void render(final VertexConsumer b, PoseStack matrixStack, final TextureAtlasSprite sprite, final ColorBox colorBox) {
        int count = Mth.floor(this.length / super.sizeY);
        float minU = sprite.getU0();
        float minV = sprite.getV0();
        float maYU = sprite.getU((float) (super.sizeY / this.maxSize));
        float maXV = sprite.getV((float) (super.sizeX / this.maxSize));
        float maZU = sprite.getU((float) (super.sizeZ / this.maxSize));
        float maZV = sprite.getV((float) (super.sizeZ / this.maxSize));
        int red = colorBox.red();
        int green = colorBox.green();
        int blue = colorBox.blue();
        int alpha = colorBox.alpha();
        Buffer buffer = new Buffer(b, matrixStack);
        if (super.firstSide) {
            buffer.pos(super.endX + this.offX, super.startY, super.endZ - this.offZ).color(red, green, blue, alpha).tex(minU, minV).lightedAndEnd();
            buffer.pos(super.endX + this.offX, super.startY, super.endZ + this.offZ).color(red, green, blue, alpha).tex(maZU, minV).lightedAndEnd();
            buffer.pos(super.endX - this.offX, super.startY, super.endZ + this.offZ).color(red, green, blue, alpha).tex(maZU, maXV).lightedAndEnd();
            buffer.pos(super.endX - this.offX, super.startY, super.endZ - this.offZ).color(red, green, blue, alpha).tex(minU, maXV).lightedAndEnd();
        }

        for (int i1 = 0; i1 <= count; ++i1) {
            double i2 = i1 == count ? this.length / super.sizeY : (double) i1 + 1.0D;
            buffer.pos(super.endX - this.offX, super.startY + super.sizeY * i2, super.endZ - this.offZ).color(red, green, blue, alpha).tex(minU, minV).lightedAndEnd();
            buffer.pos(super.endX - this.offX, super.startY + super.sizeY * (double) i1, super.endZ - this.offZ).color(red, green, blue, alpha).tex(maYU, minV).lightedAndEnd();
            buffer.pos(super.endX - this.offX, super.startY + super.sizeY * (double) i1, super.endZ + this.offZ).color(red, green, blue, alpha).tex(maYU, maZV).lightedAndEnd();
            buffer.pos(super.endX - this.offX, super.startY + super.sizeY * i2, super.endZ + this.offZ).color(red, green, blue, alpha).tex(minU, maZV).lightedAndEnd();
            buffer.pos(super.endX + this.offX, super.startY + super.sizeY * i2, super.endZ + this.offZ).color(red, green, blue, alpha).tex(minU, minV).lightedAndEnd();
            buffer.pos(super.endX + this.offX, super.startY + super.sizeY * (double) i1, super.endZ + this.offZ).color(red, green, blue, alpha).tex(maYU, minV).lightedAndEnd();
            buffer.pos(super.endX + this.offX, super.startY + super.sizeY * (double) i1, super.endZ - this.offZ).color(red, green, blue, alpha).tex(maYU, maZV).lightedAndEnd();
            buffer.pos(super.endX + this.offX, super.startY + super.sizeY * i2, super.endZ - this.offZ).color(red, green, blue, alpha).tex(minU, maZV).lightedAndEnd();
            buffer.pos(super.endX + this.offX, super.startY + super.sizeY * i2, super.endZ - this.offZ).color(red, green, blue, alpha).tex(minU, minV).lightedAndEnd();
            buffer.pos(super.endX + this.offX, super.startY + super.sizeY * (double) i1, super.endZ - this.offZ).color(red, green, blue, alpha).tex(maYU, minV).lightedAndEnd();
            buffer.pos(super.endX - this.offX, super.startY + super.sizeY * (double) i1, super.endZ - this.offZ).color(red, green, blue, alpha).tex(maYU, maXV).lightedAndEnd();
            buffer.pos(super.endX - this.offX, super.startY + super.sizeY * i2, super.endZ - this.offZ).color(red, green, blue, alpha).tex(minU, maXV).lightedAndEnd();
            buffer.pos(super.endX - this.offX, super.startY + super.sizeY * i2, super.endZ + this.offZ).color(red, green, blue, alpha).tex(minU, minV).lightedAndEnd();
            buffer.pos(super.endX - this.offX, super.startY + super.sizeY * (double) i1, super.endZ + this.offZ).color(red, green, blue, alpha).tex(maYU, minV).lightedAndEnd();
            buffer.pos(super.endX + this.offX, super.startY + super.sizeY * (double) i1, super.endZ + this.offZ).color(red, green, blue, alpha).tex(maYU, maXV).lightedAndEnd();
            buffer.pos(super.endX + this.offX, super.startY + super.sizeY * i2, super.endZ + this.offZ).color(red, green, blue, alpha).tex(minU, maXV).lightedAndEnd();
        }

        if (super.endSide) {
            buffer.pos(super.endX - this.offX, super.endY, super.endZ - this.offZ).color(red, green, blue, alpha).tex(minU, minV).lightedAndEnd();
            buffer.pos(super.endX - this.offX, super.endY, super.endZ + this.offZ).color(red, green, blue, alpha).tex(maZU, minV).lightedAndEnd();
            buffer.pos(super.endX + this.offX, super.endY, super.endZ + this.offZ).color(red, green, blue, alpha).tex(maZU, maXV).lightedAndEnd();
            buffer.pos(super.endX + this.offX, super.endY, super.endZ - this.offZ).color(red, green, blue, alpha).tex(minU, maXV).lightedAndEnd();
        }

    }
}
