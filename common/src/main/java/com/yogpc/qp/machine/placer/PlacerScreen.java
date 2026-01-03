package com.yogpc.qp.machine.placer;

import com.yogpc.qp.QuarryPlus;
import net.minecraft.client.gui.GuiGraphics;
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
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);
        super.render(graphics, mouseX, mouseY, delta);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float delta, int mouseX, int mouseY) {
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
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        renderModeLabel(graphics);
    }

    void renderModeLabel(GuiGraphics graphics) {
        // Mode
        AbstractPlacerTile.RedStoneMode mode = this.getMenu().tile.redstoneMode;
        String pA = "Pulse";
        int x = 116;
        graphics.drawString(font, pA, x, 6, ARGB.opaque(0x404040), false);
        String rs = "";
        graphics.drawString(font, rs, x, 18, ARGB.opaque(0x404040), false);
        String only;
        if (mode.canBreak() && !mode.canPlace()) only = "Break Only";
        else if (mode.canPlace() && !mode.canBreak()) only = "Place Only";
        else only = "";
        graphics.drawString(font, only, x, 30, ARGB.opaque(0x404040), false);
    }
}
