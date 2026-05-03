package com.yogpc.qp.machine.module;

import com.yogpc.qp.QuarryPlus;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Inventory;

public final class ModuleScreen extends AbstractContainerScreen<ModuleContainer> {
    private static final Identifier LOCATION = Identifier.fromNamespaceAndPath(QuarryPlus.modID, "textures/gui/quarry_module.png");

    public ModuleScreen(ModuleContainer menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractBackground(graphics, mouseX, mouseY, delta);
        int pX = leftPos;
        int pY = topPos;
        graphics.blit(RenderPipelines.GUI_TEXTURED, LOCATION, pX, pY, 0, 0, imageWidth, imageHeight, 256, 256);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
        graphics.text(font, "Modules", this.titleLabelX, this.titleLabelY + 10, ARGB.opaque(0x404040), false);
        if (getMenu().yAccessor != null) {
            graphics.text(font, "Y: " + getMenu().yAccessor.getDigMinY(), 120, this.titleLabelY, ARGB.opaque(0x404040), false);
        }
    }
}
