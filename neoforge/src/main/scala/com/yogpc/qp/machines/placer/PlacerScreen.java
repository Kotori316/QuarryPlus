package com.yogpc.qp.machines.placer;

import com.yogpc.qp.QuarryPlus;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PlacerScreen extends AbstractContainerScreen<PlacerContainer> {
    private static final ResourceLocation LOCATION = new ResourceLocation(QuarryPlus.modID, "textures/gui/replacer.png");

    public PlacerScreen(PlacerContainer c, Inventory inventory, Component component) {
        super(c, inventory, component);
    }

    protected ResourceLocation textureLocation() {
        return LOCATION;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);
        super.render(graphics, mouseX, mouseY, delta);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float delta, int mouseX, int mouseY) {
        int pX1 = getGuiLeft();
        int pY1 = getGuiTop();
        graphics.blit(textureLocation(), pX1, pY1, 0, 0, imageWidth, imageHeight);
        {
            // red = 176, 0;  start = 61, 16;
            int oneBox = 18;
            int x = getMenu().startX - 1 + (getMenu().tile.getLastPlacedIndex() % 3) * oneBox;
            int y = 16 + (getMenu().tile.getLastPlacedIndex() / 3) * oneBox;
            int pX = getGuiLeft() + x;
            int pY = getGuiTop() + y;
            graphics.blit(textureLocation(), pX, pY, 176, 0, oneBox, oneBox);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        renderModeLabel(graphics);
    }

    protected void renderModeLabel(GuiGraphics graphics) {
        // Mode
        PlacerTile.RedstoneMode mode = this.getMenu().tile.redstoneMode;
        String pA = mode.isAlways() ? "Always" : "Pulse";
        int x = 116;
        graphics.drawString(font, pA, x, 6, 0x404040, false);
        String rs;
        if (mode.isRsOn()) rs = "RS On";
        else if (mode.isRsOff()) rs = "RS Off";
        else rs = "";
        graphics.drawString(font, rs, x, 18, 0x404040, false);
        String only;
        if (mode.canBreak() && !mode.canPlace()) only = "Break Only";
        else if (mode.canPlace() && !mode.canBreak()) only = "Place Only";
        else only = "";
        graphics.drawString(font, only, x, 30, 0x404040, false);
    }
}
