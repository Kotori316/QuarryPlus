package com.yogpc.qp.machine.storage;

import com.yogpc.qp.QuarryPlus;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public final class DebugStorageScreen extends AbstractContainerScreen<DebugStorageContainer> {
    private static final Identifier LOCATION = Identifier.fromNamespaceAndPath(QuarryPlus.modID, "textures/gui/marker.png");
    ItemCountList itemCountList;

    public DebugStorageScreen(DebugStorageContainer menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, 217, 188);
        this.inventoryLabelY = this.imageHeight - 96 + 2;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractBackground(graphics, mouseX, mouseY, delta);
        int pX = leftPos;
        int pY = topPos;
        graphics.blit(RenderPipelines.GUI_TEXTURED, LOCATION, pX, pY, 0, 0, imageWidth, imageHeight, 256, 256);
    }

    @Override
    protected void init() {
        super.init();

        itemCountList = new ItemCountList(this.minecraft, 205, inventoryLabelY - 20, topPos + 18, getMenu().storage);
        itemCountList.setX(leftPos + (imageWidth - 205) / 2);
        addRenderableWidget(itemCountList);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double mouseX, double mouseY) {
        if (itemCountList.isMouseOver(mouseX, mouseY)) {
            if (itemCountList.mouseDragged(event, mouseX, mouseY)) {
                return true;
            }
        }
        return super.mouseDragged(event, mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (super.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
            return true;
        }
        return this.getChildAt(mouseX, mouseY).filter(listener -> listener.mouseScrolled(mouseX, mouseY, scrollX, scrollY)).isPresent();
    }
}
