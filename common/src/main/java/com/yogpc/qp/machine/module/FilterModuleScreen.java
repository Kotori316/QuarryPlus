package com.yogpc.qp.machine.module;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class FilterModuleScreen extends AbstractContainerScreen<FilterModuleContainer> {
    private static final ResourceLocation LOCATION = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/gui/container/generic_54.png");

    public FilterModuleScreen(FilterModuleContainer menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = 114 + menu.containerRows * 18;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        var vHeight = menu.containerRows * 18 + 17;
        guiGraphics.blit(LOCATION, leftPos, topPos, 0, 0, this.imageWidth, vHeight);
        guiGraphics.blit(LOCATION, leftPos, topPos + vHeight, 0, 126, this.imageWidth, 96);
    }
}
