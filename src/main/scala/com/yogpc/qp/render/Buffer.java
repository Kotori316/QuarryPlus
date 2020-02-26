package com.yogpc.qp.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.Vector4f;

final class Buffer {
    private final IVertexBuilder bufferBuilder;
    private final Vector4f vector4f = new Vector4f();

    Buffer(IVertexBuilder bufferBuilder) {
        this.bufferBuilder = bufferBuilder;
    }

    final Buffer pos(double x, double y, double z, MatrixStack matrix) {
        Matrix4f matrix4f = matrix.getLast().getMatrix();

        vector4f.set((float) x, (float) y, (float) z, 1.0F);
        vector4f.transform(matrix4f);
        bufferBuilder.pos(vector4f.getX(), vector4f.getY(), vector4f.getZ());
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

    @SuppressWarnings("SameParameterValue")
    final Buffer color(int red, int green, int blue, int alpha) {
        bufferBuilder.color(red, green, blue, alpha);
        return this;
    }

    final Buffer tex(float u, float v) {
        bufferBuilder.tex(u, v);
        return this;
    }

    /**
     * {@code buffer.lightmap(240, 0).endVertex()}
     */
    final void lightedAndEnd() {
        bufferBuilder.overlay(10, 10).lightmap(240, 0).normal(0, 1, 0).endVertex();
    }

    final void lightedAndEnd(Box.LightValue value) {
        bufferBuilder.overlay(10, 10).lightmap(value.l1(), value.l2()).normal(0, 1, 0).endVertex();
    }

    final boolean bufferEq(IVertexBuilder builder) {
        return bufferBuilder == builder;
    }
}
