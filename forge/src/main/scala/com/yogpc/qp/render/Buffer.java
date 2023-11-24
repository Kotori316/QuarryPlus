package com.yogpc.qp.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Matrix4f;
import org.joml.Vector4f;

final class Buffer {
    private final VertexConsumer bufferBuilder;
    private final PoseStack matrix;
    private final Vector4f vector4f = new Vector4f();
    private final ColorBox colorBox;

    Buffer(VertexConsumer bufferBuilder, PoseStack matrixStack) {
        this(bufferBuilder, matrixStack, ColorBox.white);
    }

    Buffer(VertexConsumer bufferBuilder, PoseStack matrix, ColorBox colorBox) {
        this.bufferBuilder = bufferBuilder;
        this.matrix = matrix;
        this.colorBox = colorBox;
    }

    Buffer pos(double x, double y, double z) {
        Matrix4f matrix4f = matrix.last().pose();

        vector4f.set((float) x, (float) y, (float) z, 1.0F);
        vector4f.mul(matrix4f);
        bufferBuilder.vertex(vector4f.x(), vector4f.y(), vector4f.z());
        return this;
    }

    /**
     * buffer.color(255, 255, 255, 255)
     *
     * @return this
     */
    Buffer colored() {
        return this.color(colorBox);
    }

    Buffer color(int red, int green, int blue, int alpha) {
        bufferBuilder.color(red, green, blue, alpha);
        return this;
    }

    Buffer color(ColorBox colors) {
        bufferBuilder.color(colors.red(), colors.green(), colors.blue(), colors.alpha());
        return this;
    }

    Buffer tex(float u, float v) {
        bufferBuilder.uv(u, v);
        return this;
    }

    /**
     * {@code buffer.lightmap(240, 0).endVertex()}
     */
    void lightedAndEnd() {
        bufferBuilder.overlayCoords(10, 10).uv2(240, 0).normal(0, 1, 0).endVertex();
    }

    boolean bufferEq(VertexConsumer builder) {
        return bufferBuilder == builder;
    }
}
