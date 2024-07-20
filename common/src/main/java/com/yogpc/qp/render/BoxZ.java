package com.yogpc.qp.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;

class BoxZ extends Box {

    BoxZ(final double startZ, final double endZ, final double x, final double y, final double sizeX, final double sizeY, final double sizeZ, final boolean firstSide, final boolean endSide) {
        super(x, y, startZ, x, y, endZ, sizeX, sizeY, sizeZ, firstSide, endSide);
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    void render(final VertexConsumer b, PoseStack matrixStack, final TextureAtlasSprite sprite, final ColorBox colorBox) {
        int count = Mth.floor(this.length / super.sizeZ);
        float minU = sprite.getU0();
        float minV = sprite.getV0();
        float maXU = sprite.getU((float) (super.sizeX / this.maxSize));
        float maXV = sprite.getV((float) (super.sizeX / this.maxSize));
        float maYU = sprite.getU((float) (super.sizeY / this.maxSize));
        float maZV = sprite.getV((float) (super.sizeZ / this.maxSize));
        Buffer buffer = new Buffer(b, matrixStack, colorBox);
        if (super.firstSide) {
            buffer.pos(super.endX + this.offX, super.endY + this.offY, super.startZ).colored().tex(minU, minV).lightedAndEnd();
            buffer.pos(super.endX + this.offX, super.endY - this.offY, super.startZ).colored().tex(maYU, minV).lightedAndEnd();
            buffer.pos(super.endX - this.offX, super.endY - this.offY, super.startZ).colored().tex(maYU, maXV).lightedAndEnd();
            buffer.pos(super.endX - this.offX, super.endY + this.offY, super.startZ).colored().tex(minU, maXV).lightedAndEnd();
        }

        for (int i1 = 0; i1 <= count; ++i1) {
            double i2 = i1 == count ? this.length / super.sizeZ : (double) i1 + 1.0D;
            buffer.pos(super.endX - this.offX, super.endY + this.offY, super.startZ + super.sizeZ * (double) i1).colored().tex(minU, minV).lightedAndEnd();
            buffer.pos(super.endX - this.offX, super.endY - this.offY, super.startZ + super.sizeZ * (double) i1).colored().tex(maYU, minV).lightedAndEnd();
            buffer.pos(super.endX - this.offX, super.endY - this.offY, super.startZ + super.sizeZ * i2).colored().tex(maYU, maZV).lightedAndEnd();
            buffer.pos(super.endX - this.offX, super.endY + this.offY, super.startZ + super.sizeZ * i2).colored().tex(minU, maZV).lightedAndEnd();
            buffer.pos(super.endX + this.offX, super.endY + this.offY, super.startZ + super.sizeZ * i2).colored().tex(minU, minV).lightedAndEnd();
            buffer.pos(super.endX + this.offX, super.endY - this.offY, super.startZ + super.sizeZ * i2).colored().tex(maYU, minV).lightedAndEnd();
            buffer.pos(super.endX + this.offX, super.endY - this.offY, super.startZ + super.sizeZ * (double) i1).colored().tex(maYU, maZV).lightedAndEnd();
            buffer.pos(super.endX + this.offX, super.endY + this.offY, super.startZ + super.sizeZ * (double) i1).colored().tex(minU, maZV).lightedAndEnd();
            buffer.pos(super.endX - this.offX, super.endY + this.offY, super.startZ + super.sizeZ * (double) i1).colored().tex(minU, minV).lightedAndEnd();
            buffer.pos(super.endX - this.offX, super.endY + this.offY, super.startZ + super.sizeZ * i2).colored().tex(maXU, minV).lightedAndEnd();
            buffer.pos(super.endX + this.offX, super.endY + this.offY, super.startZ + super.sizeZ * i2).colored().tex(maXU, maZV).lightedAndEnd();
            buffer.pos(super.endX + this.offX, super.endY + this.offY, super.startZ + super.sizeZ * (double) i1).colored().tex(minU, maZV).lightedAndEnd();
            buffer.pos(super.endX + this.offX, super.endY - this.offY, super.startZ + super.sizeZ * (double) i1).colored().tex(minU, minV).lightedAndEnd();
            buffer.pos(super.endX + this.offX, super.endY - this.offY, super.startZ + super.sizeZ * i2).colored().tex(maXU, minV).lightedAndEnd();
            buffer.pos(super.endX - this.offX, super.endY - this.offY, super.startZ + super.sizeZ * i2).colored().tex(maXU, maZV).lightedAndEnd();
            buffer.pos(super.endX - this.offX, super.endY - this.offY, super.startZ + super.sizeZ * (double) i1).colored().tex(minU, maZV).lightedAndEnd();
        }

        if (super.endSide) {
            buffer.pos(super.endX - this.offX, super.endY + this.offY, super.endZ).colored().tex(minU, minV).lightedAndEnd();
            buffer.pos(super.endX - this.offX, super.endY - this.offY, super.endZ).colored().tex(maYU, minV).lightedAndEnd();
            buffer.pos(super.endX + this.offX, super.endY - this.offY, super.endZ).colored().tex(maYU, maXV).lightedAndEnd();
            buffer.pos(super.endX + this.offX, super.endY + this.offY, super.endZ).colored().tex(minU, maXV).lightedAndEnd();
        }

    }
}
