package com.yogpc.qp.render;

import net.minecraft.client.renderer.VertexBuffer;

final class Buffer {
    private final VertexBuffer bufferBuilder;

    Buffer(VertexBuffer bufferBuilder) {
        this.bufferBuilder = bufferBuilder;
    }

    final Buffer pos(double x, double y, double z) {
        bufferBuilder.pos(x, y, z);
        return this;
    }

    /**
     * buffer.color(255, 255, 255, 255)
     *
     * @return this
     */
    final Buffer colored() {
        bufferBuilder.color(255, 255, 255, 255);
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
        bufferBuilder.lightmap(240, 0).endVertex();
    }

    final boolean eq(VertexBuffer builder) {
        return bufferBuilder == builder;
    }
}
