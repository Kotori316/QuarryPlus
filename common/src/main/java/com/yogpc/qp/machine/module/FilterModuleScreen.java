package com.yogpc.qp.machine.module;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public final class FilterModuleScreen extends AbstractContainerScreen<FilterModuleContainer> {
    private static final Identifier LOCATION = Identifier.fromNamespaceAndPath("minecraft", "textures/gui/container/generic_54.png");

    public FilterModuleScreen(FilterModuleContainer menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, DEFAULT_IMAGE_WIDTH, 114 + menu.containerRows * 18);
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(guiGraphics, mouseX, mouseY, partialTick);
        var vHeight = menu.containerRows * 18 + 17;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, LOCATION, leftPos, topPos, 0, 0, this.imageWidth, vHeight, 256, 256);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, LOCATION, leftPos, topPos + vHeight, 0, 126, this.imageWidth, 96, 256, 256);
    }
}
