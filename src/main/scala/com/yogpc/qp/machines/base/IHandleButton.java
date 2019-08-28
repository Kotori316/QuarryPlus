package com.yogpc.qp.machines.base;

public interface IHandleButton {
    void actionPerformed(final IHandleButton.Button button);

    class Button extends net.minecraft.client.gui.widget.button.AbstractButton {
        public final int id;
        private final IHandleButton handler;

        public Button(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, IHandleButton handler) {
            super(x, y, widthIn, heightIn, buttonText);
            this.id = buttonId;
            this.handler = handler;
        }

        @Override
        public void onPress() {
            handler.actionPerformed(this);
        }
    }
}
