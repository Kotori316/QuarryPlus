package com.yogpc.qp.machine.misc;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractStringWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public final class StringWidgetNoShadow extends AbstractStringWidget {
    public StringWidgetNoShadow(int x, int y, int width, int height, Component message, Font font) {
        super(x, y, width, height, message, font);
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Component component = this.getMessage();
        Font font = this.getFont();
        int k = this.getX();
        int l = this.getY() + (this.getHeight() - 9) / 2;
        FormattedCharSequence formattedcharsequence = component.getVisualOrderText();
        guiGraphics.drawString(font, formattedcharsequence, k, l, this.getColor(), false);
    }
}
