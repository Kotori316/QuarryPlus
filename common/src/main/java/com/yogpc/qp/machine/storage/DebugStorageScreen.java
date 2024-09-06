package com.yogpc.qp.machine.storage;

import com.yogpc.qp.QuarryPlus;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class DebugStorageScreen extends AbstractContainerScreen<DebugStorageContainer> {
    private static final ResourceLocation LOCATION = ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, "textures/gui/marker.png");

    public DebugStorageScreen(DebugStorageContainer menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        //217, 188
        this.imageWidth = 217;
        this.imageHeight = 188;
        this.inventoryLabelY = this.imageHeight - 96 + 2;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float delta, int mouseX, int mouseY) {
        int pX = leftPos;
        int pY = topPos;
        graphics.blit(LOCATION, pX, pY, 0, 0, imageWidth, imageHeight);
    }

}
