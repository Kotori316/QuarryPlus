package com.yogpc.qp.machines.bookmover;

import com.yogpc.qp.QuarryPlus;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class BookMoverScreen extends AbstractContainerScreen<BookMoverMenu> {
    private static final ResourceLocation LOCATION = new ResourceLocation(QuarryPlus.modID, "textures/gui/book_mover.png");

    public BookMoverScreen(BookMoverMenu c, Inventory inventory, Component component) {
        super(c, inventory, component);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float delta, int mouseX, int mouseY) {
        int pX1 = getGuiLeft();
        int pY1 = getGuiTop();
        graphics.blit(LOCATION, pX1, pY1, 0, 0, imageWidth, imageHeight);
        if (getMenu().moverIsWorking()) {
            int pX = getGuiLeft() + 79;
            int pY = getGuiTop() + 35;
            int pUWidth = getMenu().getProgress() * 3 / 125;
            graphics.blit(LOCATION, pX, pY, imageWidth, 14, pUWidth, 16);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);
        super.render(graphics, mouseX, mouseY, delta);
        this.renderTooltip(graphics, mouseX, mouseY);
    }
}
