package com.yogpc.qp.render;

import com.yogpc.qp.tile.TileLaser;
import com.yogpc.qp.tile.TileQuarry;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;


@SideOnly(Side.CLIENT)
public class RenderQuarry extends TileEntitySpecialRenderer<TileQuarry> {
    public static final RenderQuarry INSTANCE = new RenderQuarry();

    private RenderQuarry() {
    }

    @Override
    public void renderTileEntityAt(TileQuarry te, double x, double y, double z, float partialTicks, int destroyStage) {
        GL11.glPushMatrix();
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);// TODO lightmap
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glTranslated(x - te.getPos().getX(), y - te.getPos().getY(), z - te.getPos().getZ());
        if ((te.G_getNow() == TileQuarry.Mode.NOTNEEDBREAK || te.G_getNow() == TileQuarry.Mode.MAKEFRAME)
                && te.yMax != Integer.MIN_VALUE) {
            GL11.glPushMatrix();
            GL11.glTranslated(0.5, 0.5, 0.5);
            ResourceLocation laserTexture = TileLaser.LASER_TEXTURES[4];
            RenderLaser.renderLaser(rendererDispatcher.renderEngine, te.xMin - 0.03125, te.yMin, te.zMin, te.xMax + 0.03125, te.yMin, te.zMin, 0, laserTexture);
            RenderLaser.renderLaser(rendererDispatcher.renderEngine, te.xMin, te.yMin - 0.03125, te.zMin, te.xMin, te.yMax + 0.03125, te.zMin, 0, laserTexture);
            RenderLaser.renderLaser(rendererDispatcher.renderEngine, te.xMin, te.yMin, te.zMin - 0.03125, te.xMin, te.yMin, te.zMax + 0.03125, 0, laserTexture);
            RenderLaser.renderLaser(rendererDispatcher.renderEngine, te.xMin - 0.03125, te.yMax, te.zMax, te.xMax + 0.03125, te.yMax, te.zMax, 0, laserTexture);
            RenderLaser.renderLaser(rendererDispatcher.renderEngine, te.xMin, te.yMax + 0.03125, te.zMax, te.xMin, te.yMin - 0.03125, te.zMax, 0, laserTexture);
            RenderLaser.renderLaser(rendererDispatcher.renderEngine, te.xMin, te.yMax, te.zMax + 0.03125, te.xMin, te.yMax, te.zMin - 0.03125, 0, laserTexture);

            RenderLaser.renderLaser(rendererDispatcher.renderEngine, te.xMax + 0.03125, te.yMin, te.zMax, te.xMin - 0.03125, te.yMin, te.zMax, 0, laserTexture);
            RenderLaser.renderLaser(rendererDispatcher.renderEngine, te.xMax, te.yMin - 0.03125, te.zMax, te.xMax, te.yMax + 0.03125, te.zMax, 0, laserTexture);
            RenderLaser.renderLaser(rendererDispatcher.renderEngine, te.xMax, te.yMin, te.zMax + 0.03125, te.xMax, te.yMin, te.zMin - 0.03125, 0, laserTexture);
            RenderLaser.renderLaser(rendererDispatcher.renderEngine, te.xMax + 0.03125, te.yMax, te.zMin, te.xMin - 0.03125, te.yMax, te.zMin, 0, laserTexture);
            RenderLaser.renderLaser(rendererDispatcher.renderEngine, te.xMax, te.yMax + 0.03125, te.zMin, te.xMax, te.yMin - 0.03125, te.zMin, 0, laserTexture);
            RenderLaser.renderLaser(rendererDispatcher.renderEngine, te.xMax, te.yMax, te.zMin - 0.03125, te.xMax, te.yMax, te.zMax + 0.03125, 0, laserTexture);
            GL11.glPopMatrix();
        }
//        if (te.G_getNow() == TileQuarry.BREAKBLOCK || te.G_getNow() == TileQuarry.MOVEHEAD)
//            RenderEntityLaser.doRender(this.rendererDispatcher.renderEngine, te.xMin + 0.75, te.yMax + 0.25, te.zMin + 0.75, te.headPosX, te.headPosY, te.headPosZ, te.xMax - te.xMin - 0.5, te.zMax - te.zMin - 0.5);
        GL11.glPopAttrib();
        GL11.glPopMatrix();
    }

}
