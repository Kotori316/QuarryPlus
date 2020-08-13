package com.yogpc.qp.machines.base;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.mojang.blaze3d.matrix.MatrixStack;
import javax.annotation.Nonnull;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.IReorderingProcessor;
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
        private Supplier<List<IReorderingProcessor>> toolTipSupplier = null;
        private Screen parent;

        public Button(int buttonId, int x, int y, int widthIn, int heightIn, ITextComponent buttonText, IHandleButton handler) {
            super(x, y, widthIn, heightIn, buttonText, handler);
            this.id = buttonId;
            this.handler = handler;
        }

        public Button(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText, IHandleButton handler) {
            this(buttonId, x, y, widthIn, heightIn, new StringTextComponent(buttonText), handler);
        }

        @Override
        public void onPress() {
            handler.actionPerformed(this);
        }

        public void setToolTip(Supplier<List<ITextComponent>> toolTip, @Nonnull Screen parent) {
            this.toolTipSupplier = () -> toolTip.get().stream().map(ITextComponent::func_241878_f).collect(Collectors.toList());
            this.parent = parent;
        }

        public void setMessage(String message) {
            this.setMessage(new StringTextComponent(message));
        }

        @Override
        public void renderToolTip(MatrixStack matrixStack, int mouseX, int mouseY) {
            // render tooltip
            super.renderToolTip(matrixStack, mouseX, mouseY);
            if (parent != null && toolTipSupplier != null) {
                parent.renderTooltip(matrixStack, toolTipSupplier.get(), mouseX, mouseY);
            }
        }

    }
}
