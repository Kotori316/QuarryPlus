package com.yogpc.qp.machine.module;

import com.yogpc.qp.QuarryPlus;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class ModuleScreen extends AbstractContainerScreen<ModuleContainer> {
    private static final ResourceLocation LOCATION = ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, "textures/gui/quarry_module.png");

    public ModuleScreen(ModuleContainer menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
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

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        graphics.drawString(font, "Modules", this.titleLabelX, this.titleLabelY + 10, 0x404040, false);
        if (getMenu().yAccessor != null) {
            graphics.drawString(font, "Y: " + getMenu().yAccessor.getDigMinY(), 120, this.titleLabelY, 0x404040, false);
        }
    }
}
