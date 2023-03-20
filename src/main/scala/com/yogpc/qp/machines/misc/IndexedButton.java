package com.yogpc.qp.machines.misc;

import net.minecraft.network.chat.Component;
import net.minecraftforge.client.gui.widget.ExtendedButton;

public class IndexedButton extends ExtendedButton {

    private final int index;

    public IndexedButton(int index, int xPos, int yPos, int width, int height, Component displayString, OnPress handler) {
        super(xPos, yPos, width, height, displayString, handler);
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
