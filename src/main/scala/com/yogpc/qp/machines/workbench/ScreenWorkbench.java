package com.yogpc.qp.machines.workbench;

import com.yogpc.qp.QuarryPlus;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ScreenWorkbench extends AbstractContainerScreen<ContainerWorkbench> {

    private static final ResourceLocation LOCATION = new ResourceLocation(QuarryPlus.modID, "textures/gui/workbench.png");

    public ScreenWorkbench(ContainerWorkbench workbench, Inventory inventory, Component component) {
        super(workbench, inventory, component);
        this.imageWidth = 176;
        this.imageHeight = 222;
        this.inventoryLabelY = this.imageHeight - 96 + 2; // y position of text, inventory
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        if (getMenu().tile.getMaxEnergyStored() > 5) {
            String current = getMenu().tile.getEnergyStored() + "FE";
            graphics.drawString(font, String.format("%s/%d", current, getMenu().tile.getMaxEnergyStored()),
                    120 - this.font.width(current), this.inventoryLabelY, 0x404040, false);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float delta, int mouseX, int mouseY) {
        int pX2 = getGuiLeft();
        int pY2 = getGuiTop();
        graphics.blit(LOCATION, pX2, pY2, 0, 0, imageWidth, imageHeight);
        if (getMenu().isWorking.get() == 1) {
            int pX1 = getGuiLeft() + 8;
            int pY1 = getGuiTop() + 78;
            int pUWidth = getMenu().progress.get();
            graphics.blit(LOCATION, pX1, pY1, 0, this.imageHeight, pUWidth, 4);
            int cur_recipe = 27 + getMenu().recipeIndex.get();
            int i = (getMenu().workContinue.get() == 1 ? 16 : 0);
            int pX = getGuiLeft() + 8 + cur_recipe % 9 * 18;
            int pY = getGuiTop() + 90 + (cur_recipe / 9 - 3) * 18;
            graphics.blit(LOCATION, pX, pY, this.imageWidth + i, 0, 16, 16);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, delta);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

}
