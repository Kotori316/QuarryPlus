package com.yogpc.qp.gui;

import java.lang.reflect.Field;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.container.ContainerWorkbench;
import com.yogpc.qp.tile.TileWorkbench;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.ModelManager;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiWorkbench extends GuiContainer {
    private static final class MyFontRenderer extends FontRenderer {
        private FontRenderer p;

        public MyFontRenderer() {
            super(Minecraft.getMinecraft().gameSettings, new ResourceLocation("textures/font/ascii.png"),
                    Minecraft.getMinecraft().renderEngine, false);
        }

        @Override
        public int drawStringWithShadow(String text, float x, float y, int color) {
            int l = this.p.getStringWidth(text);
            GL11.glPushMatrix();
            GL11.glTranslatef(x, y, 0);
            if (l > 16) {
                final float f = (float) 16 / l;
                GL11.glTranslatef(l - 16, this.p.FONT_HEIGHT * (1 - f), 0);
                GL11.glScalef(f, f, 1);
            }
            l = this.p.drawStringWithShadow(text, 0, 0, color);
            GL11.glPopMatrix();
            return l;
        }

        @Override
        public int getStringWidth(final String s) {
            return this.p.getStringWidth(s);
        }

        FontRenderer setParent(final FontRenderer r) {
            this.p = r;
            return this;
        }
    }

    private static final class MyRenderItem extends RenderItem {
        private static final MyFontRenderer myfont = new MyFontRenderer();

        public MyRenderItem(TextureManager textureManager, ModelManager modelManager, ItemColors itemColors) {
            super(textureManager, modelManager, itemColors);
        }

        boolean t = true;

        @Override
        public void renderItemAndEffectIntoGUI(@Nullable EntityLivingBase livingBase, ItemStack stack, int xPosition, int yPosition) {
            if (t)
                Minecraft.getMinecraft().getRenderItem().renderItemAndEffectIntoGUI(livingBase, stack, xPosition, yPosition);
            else
                super.renderItemAndEffectIntoGUI(livingBase, stack, xPosition, yPosition);
        }

        @Override
        public void renderItemOverlayIntoGUI(FontRenderer fr, ItemStack stack, int xPosition, int yPosition, @Nullable String text) {
            if (stack.getCount() > 64)
                super.renderItemOverlayIntoGUI(myfont.setParent(fr), stack, xPosition, yPosition, text);
            else
                Minecraft.getMinecraft().getRenderItem().renderItemOverlayIntoGUI(fr, stack, xPosition, yPosition, text);
        }
    }

    private static final RenderItem myitem = new MyRenderItem(
            Minecraft.getMinecraft().getTextureManager(),
            Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getModelManager(),
            Minecraft.getMinecraft().getItemColors());

    private static final ResourceLocation gui = new ResourceLocation(QuarryPlus.modID, "textures/gui/workbench.png");
    private final TileWorkbench tile;

    public GuiWorkbench(final IInventory pi, final TileWorkbench tw) {
        super(new ContainerWorkbench(pi, tw));
        this.xSize = 176;
        this.ySize = 222;
        this.tile = tw;
    }

    @Override
    protected void drawGuiContainerForegroundLayer(final int mouseX, final int mouseY) {
        this.fontRendererObj.drawString(
                this.tile.hasCustomName() ? this.tile.getName() : I18n.format(this.tile.getName()), 8, 6, 0x404040);
        this.fontRendererObj.drawString(I18n.format("container.inventory"), 8,
                this.ySize - 96 + 2, 0x404040);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(final float partialTicks, final int mouseX, final int mouseY) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(gui);
        final int xf = this.width - this.xSize >> 1;
        final int yf = this.height - this.ySize >> 1;
        drawTexturedModalRect(xf, yf, 0, 0, this.xSize, this.ySize);
        if (tile.currentRecipe.isPresent()) {
            drawTexturedModalRect(xf + 8, yf + 78, 0, this.ySize, this.tile.getProgressScaled(160), 4);
            int cur_recipe = 27 + tile.getRecipeIndex();
            drawTexturedModalRect(xf + 8 + cur_recipe % 9 * 18, yf + 90 + (cur_recipe / 9 - 3) * 18, this.xSize, 0, 16, 16);
        }
    }

    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        handlePre();
        super.drawScreen(mouseX, mouseY, partialTicks);
        handlePost();
    }

    private static final Field item;

    static {
        //I don't know whether this works.
        Field field = null;
        try {
            Class<?> clazz = Class.forName("codechicken.nei.guihook.GuiContainerManager");
            field = clazz.getDeclaredField("drawItems");
            field.setAccessible(true);
        } catch (ReflectiveOperationException ignore) {
        }
        item = field;
    }

    private RenderItem nitem, pitem;

    public void handlePre() {
        if (item != null)
            try {
                nitem = (RenderItem) item.get(null);
                item.set(null, myitem);
            } catch (final Exception e) {
                e.printStackTrace();
            }
        pitem = itemRender;
        itemRender = myitem;
    }

    public void handlePost() {
        if (pitem != null) {
            itemRender = pitem;
            pitem = null;
        }
        if (nitem != null)
            try {
                item.set(null, nitem);
                nitem = null;
            } catch (final Exception e) {
                e.printStackTrace();
            }
    }
}
