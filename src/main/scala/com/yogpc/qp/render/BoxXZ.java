package com.yogpc.qp.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

class BoxXZ extends Box {

    BoxXZ(final double startX, final double startZ, final double endX, final double y, final double endZ, final double sizeX, final double sizeY, final double sizeZ, final boolean firstSide, final boolean endSide) {
        super(startX, y, startZ, endX, y, endZ, sizeX, sizeY, sizeZ, firstSide, endSide);
    }

    @Override
    public void render(final VertexConsumer buffer, PoseStack matrixStack, final TextureAtlasSprite sprite, final ColorBox colorBox) {
        double n2Size = this.length;
        this.renderInternal(buffer, matrixStack, sprite, 0.0D, 0.5D, 0.0D, -this.dz / n2Size / (double) 2, this.dx / n2Size / (double) 2, colorBox.alpha(), colorBox.red(), colorBox.green(), colorBox.blue());
    }

}
