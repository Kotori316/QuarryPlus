package com.yogpc.qp.render;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;

public class Box {
    final double startX;
    final double startY;
    final double startZ;
    final double endX;
    final double endY;
    final double endZ;
    final double sizeX;
    final double sizeY;
    final double sizeZ;
    final boolean firstSide;
    final boolean endSide;
    final double dx;
    final double dy;
    final double dz;
    final double lengthSq;
    final double length;
    final double offX;
    final double offY;
    final double offZ;
    final double maxSize;

    public void render(final VertexConsumer buffer, final MatrixStack matrixStack, final Sprite sprite, final ColorBox colorBox) {
        double n1X = this.dx;
        double n1Y = Box.normalY(this.dx, this.dy, this.dz);
        double n1Z = this.dz;
        double n1Size = Math.sqrt(n1X * n1X + n1Y * n1Y + n1Z * n1Z);
        double n2X = this.dy * n1Z - this.dz * n1Y;
        double n2Z = this.dx * n1Y - this.dy * n1X;
        double n2Size = Math.sqrt(n2X * n2X + n2Z * n2Z);
        this.renderInternal(buffer, matrixStack, sprite, n1X / n1Size / (double) 2, n1Y / n1Size / (double) 2, n1Z / n1Size / (double) 2,
            n2X / n2Size / (double) 2, n2Z / n2Size / (double) 2, colorBox.alpha(), colorBox.red(), colorBox.green(), colorBox.blue());
    }

    @SuppressWarnings({"UnnecessaryLocalVariable", "DuplicatedCode"})
    protected final void renderInternal(final VertexConsumer b, final MatrixStack matrixStack, final Sprite sprite, final double n1X, final double n1Y, final double n1Z,
                                        final double n2X, final double n2Z, final int alpha, final int red, final int green, final int blue) {
        double eX = this.dx / this.length * this.sizeX;
        double eY = this.dy / this.length * this.sizeY;
        double eZ = this.dz / this.length * this.sizeZ;
        double e1X = this.startX + n1X * this.sizeX + n2X * this.sizeX;
        double e1Y = this.startY + n1Y * this.sizeY;
        double e1Z = this.startZ + n1Z * this.sizeZ + n2Z * this.sizeZ;
        double e2X = this.startX - n1X * this.sizeX + n2X * this.sizeX;
        double e2Y = this.startY - n1Y * this.sizeY;
        double e2Z = this.startZ - n1Z * this.sizeZ + n2Z * this.sizeZ;
        double e3X = this.startX - n1X * this.sizeX - n2X * this.sizeX;
        double e3Y = e2Y;
        double e3Z = this.startZ - n1Z * this.sizeZ - n2Z * this.sizeZ;
        double e4X = this.startX + n1X * this.sizeX - n2X * this.sizeX;
        double e4Y = e1Y;
        double e4Z = this.startZ + n1Z * this.sizeZ - n2Z * this.sizeZ;
        Buffer buffer = new Buffer(b, matrixStack);
        if (this.firstSide) {
            buffer.pos(e1X, e1Y, e1Z).color(red, green, blue, alpha).tex(sprite.getMinU(), sprite.getMinV()).lightedAndEnd();
            buffer.pos(e2X, e2Y, e2Z).color(red, green, blue, alpha).tex(sprite.getMaxU(), sprite.getMinV()).lightedAndEnd();
            buffer.pos(e3X, e2Y, e3Z).color(red, green, blue, alpha).tex(sprite.getMaxU(), sprite.getMaxV()).lightedAndEnd();
            buffer.pos(e4X, e1Y, e4Z).color(red, green, blue, alpha).tex(sprite.getMinU(), sprite.getMaxV()).lightedAndEnd();
        }

        double l = Math.sqrt(this.dx / this.sizeX * this.dx / this.sizeX + this.dy / this.sizeY * this.dy / this.sizeY + this.dz / this.sizeZ * this.dz / this.sizeZ);
        int lengthFloor = MathHelper.floor(l);

        for (int i1 = 0; i1 <= lengthFloor; ++i1) {
            double i2 = i1 == lengthFloor ? l : (double) (i1 + 1);
            buffer.pos(e1X + eX * i2, e1Y + eY * i2, e1Z + eZ * i2).color(red, green, blue, alpha).tex(sprite.getMinU(), sprite.getMinV()).lightedAndEnd();
            buffer.pos(e1X + eX * (double) i1, e1Y + eY * (double) i1, e1Z + eZ * (double) i1).color(red, green, blue, alpha).tex(sprite.getMaxU(), sprite.getMinV()).lightedAndEnd();
            buffer.pos(e2X + eX * (double) i1, e2Y + eY * (double) i1, e2Z + eZ * (double) i1).color(red, green, blue, alpha).tex(sprite.getMaxU(), sprite.getMaxV()).lightedAndEnd();
            buffer.pos(e2X + eX * i2, e2Y + eY * i2, e2Z + eZ * i2).color(red, green, blue, alpha).tex(sprite.getMinU(), sprite.getMaxV()).lightedAndEnd();
            buffer.pos(e2X + eX * i2, e2Y + eY * i2, e2Z + eZ * i2).color(red, green, blue, alpha).tex(sprite.getMinU(), sprite.getMinV()).lightedAndEnd();
            buffer.pos(e2X + eX * (double) i1, e2Y + eY * (double) i1, e2Z + eZ * (double) i1).color(red, green, blue, alpha).tex(sprite.getMaxU(), sprite.getMinV()).lightedAndEnd();
            buffer.pos(e3X + eX * (double) i1, e3Y + eY * (double) i1, e3Z + eZ * (double) i1).color(red, green, blue, alpha).tex(sprite.getMaxU(), sprite.getMaxV()).lightedAndEnd();
            buffer.pos(e3X + eX * i2, e3Y + eY * i2, e3Z + eZ * i2).color(red, green, blue, alpha).tex(sprite.getMinU(), sprite.getMaxV()).lightedAndEnd();
            buffer.pos(e3X + eX * i2, e3Y + eY * i2, e3Z + eZ * i2).color(red, green, blue, alpha).tex(sprite.getMinU(), sprite.getMinV()).lightedAndEnd();
            buffer.pos(e3X + eX * (double) i1, e3Y + eY * (double) i1, e3Z + eZ * (double) i1).color(red, green, blue, alpha).tex(sprite.getMaxU(), sprite.getMinV()).lightedAndEnd();
            buffer.pos(e4X + eX * (double) i1, e4Y + eY * (double) i1, e4Z + eZ * (double) i1).color(red, green, blue, alpha).tex(sprite.getMaxU(), sprite.getMaxV()).lightedAndEnd();
            buffer.pos(e4X + eX * i2, e4Y + eY * i2, e4Z + eZ * i2).color(red, green, blue, alpha).tex(sprite.getMinU(), sprite.getMaxV()).lightedAndEnd();
            buffer.pos(e4X + eX * i2, e4Y + eY * i2, e4Z + eZ * i2).color(red, green, blue, alpha).tex(sprite.getMinU(), sprite.getMinV()).lightedAndEnd();
            buffer.pos(e4X + eX * (double) i1, e4Y + eY * (double) i1, e4Z + eZ * (double) i1).color(red, green, blue, alpha).tex(sprite.getMaxU(), sprite.getMinV()).lightedAndEnd();
            buffer.pos(e1X + eX * (double) i1, e1Y + eY * (double) i1, e1Z + eZ * (double) i1).color(red, green, blue, alpha).tex(sprite.getMaxU(), sprite.getMaxV()).lightedAndEnd();
            buffer.pos(e1X + eX * i2, e1Y + eY * i2, e1Z + eZ * i2).color(red, green, blue, alpha).tex(sprite.getMinU(), sprite.getMaxV()).lightedAndEnd();
        }

        if (this.endSide) {
            buffer.pos(e1X + this.dx, e1Y + this.dy, e1Z + this.dz).color(red, green, blue, alpha).tex(sprite.getMinU(), sprite.getMinV()).lightedAndEnd();
            buffer.pos(e2X + this.dx, e2Y + this.dy, e2Z + this.dz).color(red, green, blue, alpha).tex(sprite.getMaxU(), sprite.getMinV()).lightedAndEnd();
            buffer.pos(e3X + this.dx, e3Y + this.dy, e3Z + this.dz).color(red, green, blue, alpha).tex(sprite.getMaxU(), sprite.getMaxV()).lightedAndEnd();
            buffer.pos(e4X + this.dx, e4Y + this.dy, e4Z + this.dz).color(red, green, blue, alpha).tex(sprite.getMinU(), sprite.getMaxV()).lightedAndEnd();
        }

    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof Box var4) {
            return this.startX == var4.startX && this.startY == var4.startY && this.startZ == var4.startZ && this.endX == var4.endX && this.endY == var4.endY && this.endZ == var4.endZ
                && this.sizeX == var4.sizeX && this.sizeY == var4.sizeY && this.sizeZ == var4.sizeZ && this.firstSide == var4.firstSide && this.endSide == var4.endSide;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return (int) (this.startX + this.startY + this.startZ + this.endX + this.endY + this.endZ + this.sizeX + this.sizeY + this.sizeZ +
            (double) (this.firstSide ? 1 : 0) + (double) (this.endSide ? 1 : 0));
    }

    public Box(final double startX, final double startY, final double startZ, final double endX, final double endY, final double endZ,
               final double sizeX, final double sizeY, final double sizeZ, final boolean firstSide, final boolean endSide) {
        this.startX = startX;
        this.startY = startY;
        this.startZ = startZ;
        this.endX = endX;
        this.endY = endY;
        this.endZ = endZ;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.firstSide = firstSide;
        this.endSide = endSide;
        this.dx = endX - startX;
        this.dy = endY - startY;
        this.dz = endZ - startZ;
        this.lengthSq = this.dx * this.dx + this.dy * this.dy + this.dz * this.dz;
        this.length = Math.sqrt(this.lengthSq);
        this.offX = sizeX / (double) 2;
        this.offY = sizeY / (double) 2;
        this.offZ = sizeZ / (double) 2;
        this.maxSize = Math.max(Math.max(sizeX, sizeY), sizeZ);
    }

    public static double normalY(final double x, final double y, final double z) {
        return -(x * x + z * z) / y;
    }

    public static Box apply(final net.minecraft.util.math.Box axisAlignedBB, final double sizeX, final double sizeY, final double sizeZ, final boolean firstSide, final boolean endSide) {
        return Box.apply(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ, axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ, sizeX, sizeY, sizeZ, firstSide, endSide);
    }

    public static Box apply(final double startX, final double startY, final double startZ, final double endX, final double endY, final double endZ,
                            final double sizeX, final double sizeY, final double sizeZ, final boolean firstSide, final boolean endSide) {
        return startY == endY ? (startX == endX ? new BoxZ(startZ, endZ, endX, endY, sizeX, sizeY, sizeZ, firstSide, endSide)
            : (startZ == endZ ? new BoxX(startX, endX, endY, endZ, sizeX, sizeY, sizeZ, firstSide, endSide)
            : new BoxXZ(startX, startZ, endX, endY, endZ, sizeX, sizeY, sizeZ, firstSide, endSide)))
            : (startZ == endZ && startX == endX ? new BoxY(startY, endY, endX, endZ, sizeX, sizeY, sizeZ, firstSide, endSide)
            : new Box(startX, startY, startZ, endX, endY, endZ, sizeX, sizeY, sizeZ, firstSide, endSide));
    }

}
