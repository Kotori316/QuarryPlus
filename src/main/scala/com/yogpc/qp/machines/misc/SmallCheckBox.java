package com.yogpc.qp.machines.misc;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class SmallCheckBox extends Button {
    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/checkbox.png");
    private final int checkBoxWidth;
    private final int checkBoxHeight;
    private boolean selected;

    public SmallCheckBox(int xPos, int yPos, int totalWidth, int totalHeight, int checkBoxWidth, int checkBoxHeight, Component displayString, boolean selected, Button.OnPress onPress) {
        this(xPos, yPos, totalWidth, totalHeight, checkBoxWidth, checkBoxHeight, displayString, selected, onPress, DEFAULT_NARRATION);
    }

    public SmallCheckBox(int xPos, int yPos, int totalWidth, int totalHeight, int checkBoxWidth, int checkBoxHeight,
                         Component displayString, boolean selected, Button.OnPress onPress, Button.CreateNarration narration) {
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
    public void onPress() {
        this.selected = !this.selected;
        super.onPress();
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int pMouseX, int pMouseY, float pPartialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        RenderSystem.enableDepthTest();
        graphics.setColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        Font font = minecraft.font;
        float uOffset = this.isHoveredOrFocused() ? 20.0F : 0.0F;
        float vOffset = this.isSelected() ? 20.0F : 0.0F;
        graphics.blit(TEXTURE, this.getX(), this.getY() + this.height / 2 - this.checkBoxHeight / 2,
            this.checkBoxWidth, this.checkBoxHeight, uOffset, vOffset, 20, 20, 64, 64);
        // this.renderBg(pPoseStack, minecraft, pMouseX, pMouseY);
        int color = 0x404040;
        int labelOffset = this.checkBoxWidth / 5;
        graphics.drawString(font, this.getMessage(), this.getX() + this.checkBoxWidth + labelOffset, this.getY() + (this.height - 7) / 2,
            color | Mth.ceil(this.alpha * 255.0F) << 24, true);
    }
}
