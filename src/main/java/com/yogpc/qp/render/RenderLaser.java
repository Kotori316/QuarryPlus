package com.yogpc.qp.render;

import com.yogpc.qp.block.BlockLaser;
import com.yogpc.qp.tile.TileLaser;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;


@SideOnly(Side.CLIENT)
public class RenderLaser extends TileEntitySpecialRenderer<TileLaser> {
    private static final ModelBase model = new ModelBase() {
    };
    private static final ModelRenderer[] box = new ModelRenderer[40];

    static {
        for (int k = 0; k < box.length; ++k) {
            box[k] = new ModelRenderer(model, box.length - k, 0);
            box[k].addBox(0, -0.5F, -0.5F, 16, 1, 1);
        }
    }

    public static final RenderLaser INSTANCE = new RenderLaser();

    private RenderLaser() {
    }

    static void renderLaser(final TextureManager tm, final double fx, final double fy,
                            final double fz, final double tx, final double ty, final double tz, final int b,
                            final ResourceLocation tex) {
        GL11.glPushMatrix();
        GL11.glTranslated(tx, ty, tz);
        final double dx = tx - fx, dy = ty - fy, dz = tz - fz;
        final double total = Math.sqrt(dx * dx + dy * dy + dz * dz);
        GL11.glRotatef((float) (360 - (Math.atan2(dz, dx) * 180.0 / Math.PI + 180.0)), 0, 1, 0);
        GL11.glRotatef((float) (-Math.atan2(dy, Math.sqrt(total * total - dy * dy)) * 180.0 / Math.PI), 0, 0, 1);
        tm.bindTexture(tex);
        int i = 0;
        while (i <= total - 1) {
            box[b].render(1F / 16);
            GL11.glTranslated(1, 0, 0);
            i++;
        }
        GL11.glScaled(total - i, 1, 1);
        box[b].render(1F / 16);
        GL11.glPopMatrix();
    }

    @Override
    public void renderTileEntityAt(TileLaser laser, double x, double y, double z, float partialTicks, int destroyStage) {
        if (laser != null && laser.lasers != null) {
            GL11.glPushMatrix();
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);// TODO lightmap
            GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glEnable(GL11.GL_LIGHTING);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            GL11.glTranslated(x - laser.getPos().getX(), y - laser.getPos().getY(), z - laser.getPos().getZ());
            for (final TileLaser.Position l : laser.lasers) {
                EnumFacing facing = laser.getWorld().getBlockState(laser.getPos()).getValue(BlockLaser.FACING);
                renderLaser(this.rendererDispatcher.renderEngine, laser.getPos().getX() + 0.5 + 0.3 * facing.getFrontOffsetX(),
                        laser.getPos().getY() + 0.5 + 0.3 * facing.getFrontOffsetY(), laser.getPos().getZ() + 0.5 + 0.3 * facing.getFrontOffsetZ(), l.x, l.y, l.z,
                        (int) (laser.getWorld().getWorldTime() % 40), laser.getTexture());
            }
            GL11.glPopAttrib();
            GL11.glPopMatrix();
        }
    }

}
