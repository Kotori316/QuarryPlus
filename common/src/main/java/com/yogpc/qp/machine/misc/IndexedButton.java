package com.yogpc.qp.machine.misc;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class IndexedButton extends Button {

    private final int index;

    public IndexedButton(int index, int xPos, int yPos, int width, int height, Component displayString, Button.OnPress handler, CreateNarration createNarration) {
        super(xPos, yPos, width, height, displayString, handler, createNarration);
        this.index = index;
    }

    public IndexedButton(int index, int xPos, int yPos, int width, int height, Component displayString, Button.OnPress handler) {
        this(index, xPos, yPos, width, height, displayString, handler, Button.DEFAULT_NARRATION);
    }

    public int getIndex() {
        return index;
    }

    public int id() {
        return getIndex();
    }

    @Override
    public boolean isHoveredOrFocused() {
        return isHovered();
    }

    /**
     * Same content as {@link net.minecraft.client.gui.components.Button.Plain}
     */
    @Override
    protected void extractContents(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.extractDefaultSprite(guiGraphics);
        this.extractDefaultLabel(guiGraphics.textRendererForWidget(this, GuiGraphicsExtractor.HoveredTextEffects.NONE));
    }
}
