package com.yogpc.qp.gui;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.container.ContainerSolidQuarry;
import com.yogpc.qp.tile.TileSolidQuarry;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class GuiSolidQuarry extends GuiContainer {

    private static final ResourceLocation LOCATION = new ResourceLocation(QuarryPlus.modID, "textures/gui/solidquarry.png");

    public GuiSolidQuarry(TileSolidQuarry quarry, EntityPlayer player) {
        super(new ContainerSolidQuarry(quarry, player));
    }

    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(LOCATION);
        this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(final int mouseX, final int mouseY) {
        this.fontRenderer.drawString(I18n.format(TranslationKeys.solidquarry), 8, 6, 0x404040);
        this.fontRenderer.drawString(I18n.format(TranslationKeys.CONTAINER_INVENTORY), 8, this.ySize - 96 + 2, 0x404040);
    }
}
