package com.yogpc.qp.machines.base;

import java.util.List;
import java.util.function.Supplier;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public interface IHandleButton extends Button.IPressable {
    void actionPerformed(final IHandleButton.Button button);

    @Override
    default void onPress(net.minecraft.client.gui.widget.button.Button b) {
        actionPerformed(((Button) b));
    }

    class Button extends net.minecraft.client.gui.widget.button.Button {
        public final int id;
        private final IHandleButton handler;
        private Supplier<List<ITextComponent>> toolTipSupplier = null;
        private Screen parent;

        public Button(int buttonId, int x, int y, int widthIn, int heightIn, ITextComponent buttonText, IHandleButton handler) {
            super(x, y, widthIn, heightIn, buttonText, handler);
            this.id = buttonId;
            this.handler = handler;
        }

        public Button(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, IHandleButton handler) {
            this(buttonId, x, y, widthIn, heightIn, new StringTextComponent(buttonText), handler);
        }

        public void onPress() {
            handler.actionPerformed(this);
        }

        public void setToolTip(Supplier<List<ITextComponent>> toolTip, @Nonnull Screen parent) {
            this.toolTipSupplier = toolTip;
            this.parent = parent;
        }

        public void setMessage(String message) {
            this.func_238482_a_(new StringTextComponent(message));
        }

        @Override
        public void func_230443_a_(MatrixStack matrixStack, int mouseX, int mouseY) {
            // render tooltip
            super.func_230443_a_(matrixStack, mouseX, mouseY);
            if (parent != null && toolTipSupplier != null) {
                parent.func_238654_b_(matrixStack, toolTipSupplier.get(), mouseX, mouseY);
            }
        }

        @Override
        public void func_230930_b_() {
            onPress();
        }

    }
}
