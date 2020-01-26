package com.yogpc.qp.machines.base;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.client.gui.screen.Screen;

public interface IHandleButton {
    void actionPerformed(final IHandleButton.Button button);

    class Button extends net.minecraft.client.gui.widget.button.AbstractButton {
        public final int id;
        private final IHandleButton handler;
        private Supplier<List<String>> toolTipSupplier = null;
        private Screen parent;

        public Button(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, IHandleButton handler) {
            super(x, y, widthIn, heightIn, buttonText);
            this.id = buttonId;
            this.handler = handler;
        }

        @Override
        public void onPress() {
            handler.actionPerformed(this);
        }

        public void setToolTip(Supplier<List<String>> toolTip, Screen parent) {
            this.toolTipSupplier = toolTip;
            this.parent = parent;
        }

        @Override
        public void renderToolTip(int mouseX, int mouseY) {
            super.renderToolTip(mouseX, mouseY);
            if (parent != null && toolTipSupplier != null) {
                parent.renderTooltip(toolTipSupplier.get(), mouseX, mouseY);
            }
        }
    }
}
