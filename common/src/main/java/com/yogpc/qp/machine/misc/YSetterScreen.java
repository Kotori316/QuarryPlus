package com.yogpc.qp.machine.misc;

import com.yogpc.qp.QuarryPlus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Inventory;

public final class YSetterScreen extends AbstractContainerScreen<YSetterContainer> {
    private static final Identifier LOCATION = Identifier.fromNamespaceAndPath(QuarryPlus.modID, "textures/gui/adv_pump.png");
    private static final int tp = 15;

    public YSetterScreen(YSetterContainer handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
        if (getMenu().yAccessor != null) {
            var level = String.valueOf(getMenu().yAccessor.getDigMinY());
            graphics.text(font, level, (this.imageWidth - font.width(level)) / 2, tp + 23, ARGB.opaque(0x404040), false);
        }
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
            int n = Minecraft.getInstance().hasShiftDown() ? 16 : Minecraft.getInstance().hasControlDown() ? 4 : 1;
            var count = (plus ? 1 : -1) * n;
            var updated = Math.min(count + accessor.getDigMinY(), accessor.getLimitTop());
            accessor.setDigMinY(updated);
            accessor.syncToServer();
        }
    }
}
