package com.yogpc.qp.machine.misc;

import com.yogpc.qp.QuarryPlus;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class YSetterScreen extends AbstractContainerScreen<YSetterContainer> {
    private static final ResourceLocation LOCATION = ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, "textures/gui/adv_pump.png");
    private static final int tp = 15;

    public YSetterScreen(YSetterContainer handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(graphics, mouseX, mouseY, delta);
        super.render(graphics, mouseX, mouseY, delta);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        if (getMenu().yAccessor != null) {
            var level = String.valueOf(getMenu().yAccessor.getDigMinY());
            graphics.drawString(font, level, (this.imageWidth - font.width(level)) / 2, tp + 23, 0x404040, false);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float delta, int mouseX, int mouseY) {
        int pX = leftPos;
        int pY = topPos;
        graphics.blit(LOCATION, pX, pY, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void init() {
        super.init();
        final int width = 40;
        addRenderableWidget(Button.builder(Component.literal("+"), b -> changeDigY(true))
            .pos(this.leftPos + this.imageWidth / 2 - width / 2, this.topPos + tp)
            .size(width, 20)
            .build());
        addRenderableWidget(Button.builder(Component.literal("-"), b -> changeDigY(false))
            .pos(this.leftPos + this.imageWidth / 2 - width / 2, this.topPos + tp + 33)
            .size(width, 20)
            .build());
    }

    private void changeDigY(boolean plus) {
        var accessor = getMenu().yAccessor;
        if (accessor != null) {
            int n = Screen.hasShiftDown() ? 16 : Screen.hasControlDown() ? 4 : 1;
            var count = (plus ? 1 : -1) * n;
            var updated = Math.min(count + accessor.getDigMinY(), accessor.getLimitTop());
            accessor.setDigMinY(updated);
            accessor.syncToServer();
        }
    }
}
