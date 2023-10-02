package com.yogpc.qp.machines.module;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class FilterModuleScreen extends AbstractContainerScreen<FilterModuleMenu> {
    private static final ResourceLocation LOCATION = new ResourceLocation("minecraft", "textures/gui/container/generic_54.png");
    private final int containerRows;

    public FilterModuleScreen(FilterModuleMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.containerRows = 2;
        this.imageHeight = 114 + this.containerRows * 18;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float pPartialTick, int pMouseX, int pMouseY) {
        int pX1 = getGuiLeft();
        int pY1 = getGuiTop();
        graphics.blit(LOCATION, pX1, pY1, 0, 0, this.imageWidth, this.containerRows * 18 + 17);
        int pX = getGuiLeft();
        int pY = getGuiTop() + this.containerRows * 18 + 17;
        graphics.blit(LOCATION, pX, pY, 0, 126, this.imageWidth, 96);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);
        super.render(graphics, mouseX, mouseY, delta);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

}
