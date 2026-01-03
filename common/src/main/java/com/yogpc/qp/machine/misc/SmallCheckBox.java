package com.yogpc.qp.machine.misc;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

public final class SmallCheckBox extends Button {
    private static final Identifier CHECKBOX_SELECTED_HIGHLIGHTED_SPRITE = Identifier.withDefaultNamespace("widget/checkbox_selected_highlighted");
    private static final Identifier CHECKBOX_SELECTED_SPRITE = Identifier.withDefaultNamespace("widget/checkbox_selected");
    private static final Identifier CHECKBOX_HIGHLIGHTED_SPRITE = Identifier.withDefaultNamespace("widget/checkbox_highlighted");
    private static final Identifier CHECKBOX_SPRITE = Identifier.withDefaultNamespace("widget/checkbox");
    private final int checkBoxWidth;
    private final int checkBoxHeight;
    private boolean selected;

    public SmallCheckBox(int xPos, int yPos, int totalWidth, int totalHeight, int checkBoxWidth, int checkBoxHeight, Component displayString, boolean selected, OnPress onPress) {
        this(xPos, yPos, totalWidth, totalHeight, checkBoxWidth, checkBoxHeight, displayString, selected, onPress, DEFAULT_NARRATION);
    }

    public SmallCheckBox(int xPos, int yPos, int totalWidth, int totalHeight, int checkBoxWidth, int checkBoxHeight,
                         Component displayString, boolean selected, OnPress onPress, CreateNarration narration) {
        super(xPos, yPos, totalWidth, totalHeight, displayString, onPress, narration);
        this.checkBoxWidth = checkBoxWidth;
        this.checkBoxHeight = checkBoxHeight;
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public void onPress(InputWithModifiers input) {
        this.selected = !this.selected;
        super.onPress(input);
    }

    @Override
    protected void renderContents(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        // RenderSystem.enableDepthTest();
        // RenderSystem.enableBlend();
        Font font = minecraft.font;
        Identifier identifier;
        if (isSelected()) {
            identifier = this.isMouseOver(pMouseX, pMouseY) ? CHECKBOX_SELECTED_HIGHLIGHTED_SPRITE : CHECKBOX_SELECTED_SPRITE;
        } else {
            identifier = this.isMouseOver(pMouseX, pMouseY) ? CHECKBOX_HIGHLIGHTED_SPRITE : CHECKBOX_SPRITE;
        }
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, identifier, this.getX(), this.getY(), this.checkBoxWidth, this.checkBoxHeight, ARGB.white(this.alpha));
        // this.renderBg(pPoseStack, minecraft, pMouseX, pMouseY);
        int color = 0x404040;
        int labelOffset = this.checkBoxWidth / 5;
        graphics.drawString(font, this.getMessage(), this.getX() + this.checkBoxWidth + labelOffset, this.getY() + (this.height - 7) / 2,
            color | Mth.ceil(this.alpha * 255.0F) << 24, false);
    }
}
