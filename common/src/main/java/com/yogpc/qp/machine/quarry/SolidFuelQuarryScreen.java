package com.yogpc.qp.machine.quarry;

import com.yogpc.qp.QuarryPlus;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Inventory;

public class SolidFuelQuarryScreen extends AbstractContainerScreen<SolidFuelQuarryMenu> {
    private static final Identifier LOCATION = Identifier.fromNamespaceAndPath(QuarryPlus.modID, "textures/gui/solid_fuel_quarry.png");

    public SolidFuelQuarryScreen(SolidFuelQuarryMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractBackground(graphics, mouseX, mouseY, delta);
        graphics.blit(RenderPipelines.GUI_TEXTURED, LOCATION, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
        graphics.text(font, "FuelCount: " + getMenu().fuelCountData.get(), titleLabelX, titleLabelY + 10, ARGB.opaque(0x404040), false);
        // Align to the right edge, so that the position of "FE" doesn't move when the number of digits changes. The position is out of the fuel slot.
        String energy = getMenu().energyData.get() + " FE";
        graphics.text(font, energy, imageWidth - titleLabelX - font.width(energy), titleLabelY, ARGB.opaque(0x404040), false);
    }
}
