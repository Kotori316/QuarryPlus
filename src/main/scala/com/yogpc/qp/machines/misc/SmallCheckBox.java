package com.yogpc.qp.machines.misc;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
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
    public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        Font font = minecraft.font;
        float uOffset = this.isHoveredOrFocused() ? 20.0F : 0.0F;
        float vOffset = this.isSelected() ? 20.0F : 0.0F;
        blit(pPoseStack, this.getX(), this.getY() + this.height / 2 - this.checkBoxHeight / 2,
            this.checkBoxWidth, this.checkBoxHeight, uOffset, vOffset, 20, 20, 64, 64);
        this.renderBg(pPoseStack, minecraft, pMouseX, pMouseY);
        int color = 0x404040;
        int labelOffset = this.checkBoxWidth / 5;
        font.draw(pPoseStack, this.getMessage(), this.getX() + this.checkBoxWidth + labelOffset, this.getY() + ((float) this.height - 7) / 2,
            color | Mth.ceil(this.alpha * 255.0F) << 24);
    }
}
