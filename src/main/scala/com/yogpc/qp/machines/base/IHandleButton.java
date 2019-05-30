package com.yogpc.qp.machines.base;

import net.minecraft.client.gui.GuiButton;

public interface IHandleButton {
    void actionPerformed(final GuiButton button);

    class Button extends GuiButton {
        private final IHandleButton handler;

        public Button(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, IHandleButton handler) {
            super(buttonId, x, y, widthIn, heightIn, buttonText);
            this.handler = handler;
        }

        @Override
        public final void onClick(double mouseX, double mouseY) {
            handler.actionPerformed(this);
        }
    }
}
