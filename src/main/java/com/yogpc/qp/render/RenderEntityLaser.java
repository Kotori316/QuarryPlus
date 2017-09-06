package com.yogpc.qp.render;

import com.yogpc.qp.entity.EntityLaser;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderEntityLaser extends Render<EntityLaser> {

    public RenderEntityLaser(RenderManager renderManager) {
        super(renderManager);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityLaser entity) {
        return entity.texture.location();
    }

    @Override
    public void doRender(EntityLaser entity, double posX, double posY, double posZ, float entityYaw, float partialTicks) {
        super.doRender(entity, posX, posY, posZ, entityYaw, partialTicks);
        AxisAlignedBB boundingBox = entity.getEntityBoundingBox();
        double x = posX - entity.lastTickPosX;
        double y = posY - entity.lastTickPosY;
        double z = posZ - entity.lastTickPosZ;

        EntityLaser.LaserType laser = entity.texture;
        bindEntityTexture(entity);
        doRender(boundingBox, x, y, z, laser);
    }

    private static void doRender(AxisAlignedBB boundingBox, double x, double y, double z, EntityLaser.LaserType laser) {
        Tessellator tessellator = Tessellator.getInstance();
        VertexBuffer vertexbuffer = tessellator.getBuffer();

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.color(laser.getr(), laser.getg(), laser.getb(), laser.geta());
        vertexbuffer.setTranslation(x, y, z);
        vertexbuffer.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
        vertexbuffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).normal(0.0F, 0.0F, -1.0F).endVertex();
        vertexbuffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).normal(0.0F, 0.0F, -1.0F).endVertex();
        vertexbuffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).normal(0.0F, 0.0F, -1.0F).endVertex();
        vertexbuffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).normal(0.0F, 0.0F, -1.0F).endVertex();
        vertexbuffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).normal(0.0F, 0.0F, 1.0F).endVertex();
        vertexbuffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).normal(0.0F, 0.0F, 1.0F).endVertex();
        vertexbuffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).normal(0.0F, 0.0F, 1.0F).endVertex();
        vertexbuffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).normal(0.0F, 0.0F, 1.0F).endVertex();
        vertexbuffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).normal(0.0F, -1.0F, 0.0F).endVertex();
        vertexbuffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).normal(0.0F, -1.0F, 0.0F).endVertex();
        vertexbuffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).normal(0.0F, -1.0F, 0.0F).endVertex();
        vertexbuffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).normal(0.0F, -1.0F, 0.0F).endVertex();
        vertexbuffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).normal(0.0F, 1.0F, 0.0F).endVertex();
        vertexbuffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).normal(0.0F, 1.0F, 0.0F).endVertex();
        vertexbuffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).normal(0.0F, 1.0F, 0.0F).endVertex();
        vertexbuffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).normal(0.0F, 1.0F, 0.0F).endVertex();
        vertexbuffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).normal(-1.0F, 0.0F, 0.0F).endVertex();
        vertexbuffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).normal(-1.0F, 0.0F, 0.0F).endVertex();
        vertexbuffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).normal(-1.0F, 0.0F, 0.0F).endVertex();
        vertexbuffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).normal(-1.0F, 0.0F, 0.0F).endVertex();
        vertexbuffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).normal(1.0F, 0.0F, 0.0F).endVertex();
        vertexbuffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).normal(1.0F, 0.0F, 0.0F).endVertex();
        vertexbuffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).normal(1.0F, 0.0F, 0.0F).endVertex();
        vertexbuffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).normal(1.0F, 0.0F, 0.0F).endVertex();
        tessellator.draw();
        vertexbuffer.setTranslation(0.0D, 0.0D, 0.0D);
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

    /*
    @Override
    public void doRender(final EntityLaser e, final double i, final double j, final double k, final float f, final float f1) {
        if (e.isDead)
            return;
        GL11.glPushMatrix();
        GL11.glTranslated(i - e.posX, j - e.posY, k - e.posZ);
        doRender(this.renderManager.renderEngine, e);
        GL11.glPopMatrix();
    }

    static void doRender(final TextureManager tm, final double posX, final double posY,
                         final double posZ, final double headX, final double headY, final double headZ,
                         final double armXSize, final double armZSize) {
        doRender(tm, posX, posY, headZ + 0.25, armXSize, 0.5, 0.5, EntityLaser.DRILL);
        doRender(tm, headX + 0.25, headY + 1, headZ + 0.25, 0.5, posY - headY - 1, 0.5,
                EntityLaser.DRILL);
        doRender(tm, headX + 0.25, posY, posZ, 0.5, 0.5, armZSize, EntityLaser.DRILL);
        doRender(tm, headX + 0.4, headY, headZ + 0.4, 0.2, 1, 0.2, EntityLaser.DRILL_HEAD);
    }

    private static void doRender(final TextureManager tm, final EntityLaser ed) {
        doRender(tm, ed.posX, ed.posY, ed.posZ, ed.iSize, ed.jSize, ed.kSize, ed.texture);
    }

    private static void doRender(final TextureManager tm, final double i, final double j,
                                 final double k, final double iSize, final double jSize, final double kSize, final int tex) {
        GL11.glPushMatrix();
        GL11.glTranslated(i, j, k);
        tm.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        final Tessellator t = Tessellator.getInstance();
        final RenderBlocks rb = new RenderBlocks();
        for (int iBase = 0; iBase < iSize; ++iBase)
            for (int jBase = 0; jBase < jSize; ++jBase)
                for (int kBase = 0; kBase < kSize; ++kBase) {
                    final double remainX = iSize - iBase;
                    final double remainY = jSize - jBase;
                    final double remainZ = kSize - kBase;
                    GL11.glPushMatrix();
                    GL11.glTranslatef(iBase, jBase, kBase);
                    IIcon texture = icons[tex];
                    if (texture == null)
                        texture = Blocks.sand.getBlockTextureFromSide(0);
                    t.startDrawingQuads();
                    rb.setRenderBounds(0, 0, 0, remainX > 1.0 ? 1.0 : remainX, remainY > 1.0 ? 1.0 : remainY,
                            remainZ > 1.0 ? 1.0 : remainZ);
                    rb.renderFaceYNeg(Blocks.sand, 0, 0, 0, texture);
                    rb.renderFaceYPos(Blocks.sand, 0, 0, 0, texture);
                    rb.renderFaceZNeg(Blocks.sand, 0, 0, 0, texture);
                    rb.renderFaceZPos(Blocks.sand, 0, 0, 0, texture);
                    rb.renderFaceXNeg(Blocks.sand, 0, 0, 0, texture);
                    rb.renderFaceXPos(Blocks.sand, 0, 0, 0, texture);
                    t.draw();
                    GL11.glPopMatrix();

                }
        GL11.glPopMatrix();
    }*/
}
