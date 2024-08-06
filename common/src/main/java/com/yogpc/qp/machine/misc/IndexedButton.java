package com.yogpc.qp.machine.misc;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class IndexedButton extends Button {

    private final int index;

    public IndexedButton(int index, int xPos, int yPos, int width, int height, Component displayString, Button.OnPress handler, CreateNarration createNarration) {
        super(xPos, yPos, width, height, displayString, handler, createNarration);
        this.index = index;
    }

    public IndexedButton(int index, int xPos, int yPos, int width, int height, Component displayString, Button.OnPress handler) {
        super(xPos, yPos, width, height, displayString, handler, Button.DEFAULT_NARRATION);
        this.index = index;
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
}
