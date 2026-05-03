package com.yogpc.qp.machine.placer;

import com.yogpc.qp.QuarryPlus;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Inventory;

public final class PlacerScreen extends AbstractContainerScreen<PlacerContainer> {
    private static final Identifier LOCATION = Identifier.fromNamespaceAndPath(QuarryPlus.modID, "textures/gui/replacer.png");

    public PlacerScreen(PlacerContainer c, Inventory inventory, Component component) {
        super(c, inventory, component);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        super.extractBackground(graphics, mouseX, mouseY, delta);
        graphics.blit(RenderPipelines.GUI_TEXTURED, LOCATION, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        {
            // red = 176, 0;  start = 61, 16;
            int oneBox = 18;
            int x = getMenu().startX - 1 + (getMenu().tile.getLastPlacedIndex() % 3) * oneBox;
            int y = 16 + (getMenu().tile.getLastPlacedIndex() / 3) * oneBox;
            int pX = leftPos + x;
            int pY = topPos + y;
            graphics.blit(RenderPipelines.GUI_TEXTURED, LOCATION, pX, pY, 176, 0, oneBox, oneBox, 256, 256);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
        renderModeLabel(graphics);
    }

    void renderModeLabel(GuiGraphicsExtractor graphics) {
        // Mode
        AbstractPlacerTile.RedStoneMode mode = this.getMenu().tile.redstoneMode;
        String pA = "Pulse";
        int x = 116;
        graphics.text(font, pA, x, 6, ARGB.opaque(0x404040), false);
        String rs = "";
        graphics.text(font, rs, x, 18, ARGB.opaque(0x404040), false);
        String only;
        if (mode.canBreak() && !mode.canPlace()) only = "Break Only";
        else if (mode.canPlace() && !mode.canBreak()) only = "Place Only";
        else only = "";
        graphics.text(font, only, x, 30, ARGB.opaque(0x404040), false);
    }
}
