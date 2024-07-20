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

    Buffer(VertexConsumer bufferBuilder, PoseStack matrix, ColorBox colorBox) {
        this.bufferBuilder = bufferBuilder;
        this.matrix = matrix;
        this.colorBox = colorBox;
    }

    Buffer pos(double x, double y, double z) {
        Matrix4f matrix4f = matrix.last().pose();

        vector4f.set((float) x, (float) y, (float) z, 1.0F);
        vector4f.mul(matrix4f);
        bufferBuilder.addVertex(vector4f.x(), vector4f.y(), vector4f.z());
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
        bufferBuilder.setColor(red, green, blue, alpha);
        return this;
    }

    Buffer color(ColorBox colors) {
        return color(colors.red(), colors.green(), colors.blue(), colors.alpha());
    }

    Buffer tex(float u, float v) {
        bufferBuilder.setUv(u, v);
        return this;
    }

    void lightedAndEnd() {
        bufferBuilder.setUv1(10, 10).setUv2(240, 0).setNormal(0, 1, 0);
    }
}
