package com.yogpc.qp.render;

import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vector4f;

final class Buffer {
    private final VertexConsumer bufferBuilder;
    private final MatrixStack matrix;
    private final Vector4f vector4f = new Vector4f();

    Buffer(VertexConsumer bufferBuilder, MatrixStack matrixStack) {
        this.bufferBuilder = bufferBuilder;
        matrix = matrixStack;
    }

    final Buffer pos(double x, double y, double z) {
        Matrix4f matrix4f = matrix.peek().getModel();

        vector4f.set((float) x, (float) y, (float) z, 1.0F);
        vector4f.transform(matrix4f);
        bufferBuilder.vertex(vector4f.getX(), vector4f.getY(), vector4f.getZ());
        return this;
    }

    /**
     * buffer.color(255, 255, 255, 255)
     *
     * @return this
     */
    final Buffer colored() {
        return this.color(255, 255, 255, 255);
    }

    final Buffer color(int red, int green, int blue, int alpha) {
        bufferBuilder.color(red, green, blue, alpha);
        return this;
    }

    final Buffer tex(float u, float v) {
        bufferBuilder.texture(u, v);
        return this;
    }

    /**
     * {@code buffer.lightmap(240, 0).endVertex()}
     */
    final void lightedAndEnd() {
        bufferBuilder.overlay(10, 10).light(240, 0).normal(0, 1, 0).next();
    }

    final boolean bufferEq(VertexConsumer builder) {
        return bufferBuilder == builder;
    }
}
