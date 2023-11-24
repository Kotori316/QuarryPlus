package com.yogpc.qp.machines.misc;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;

import java.util.function.Function;

public class IndexedButton extends ExtendedButton {

    private final int index;

    public IndexedButton(int index, int xPos, int yPos, int width, int height, Component displayString, OnPress handler) {
        super(xPos, yPos, width, height, displayString, handler);
        this.index = index;
    }

    private IndexedButton(int index, Button.Builder builder) {
        super(builder);
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

    public static Function<Button.Builder, Button> builder(final int buttonIndex) {
        return b -> new IndexedButton(buttonIndex, b);
    }
}
