package com.yogpc.qp.machines.advpump;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.misc.IndexedButton;
import com.yogpc.qp.packet.PacketHandler;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class AdvPumpScreen extends AbstractContainerScreen<AdvPumpMenu> implements Button.OnPress {
    private static final ResourceLocation LOCATION = new ResourceLocation(QuarryPlus.modID, "textures/gui/adv_pump.png");

    public AdvPumpScreen(AdvPumpMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        this.renderTooltip(matrices, mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, LOCATION);
        this.blit(matrices, getGuiLeft(), getGuiTop(), 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(PoseStack pPoseStack, int pMouseX, int pMouseY) {
        super.renderLabels(pPoseStack, pMouseX, pMouseY);
    }

    @Override
    protected void init() {
        super.init();
        final int buttonWidth = 80;
        this.addRenderableWidget(new IndexedButton(0, getGuiLeft() + getXSize() / 2 - buttonWidth, getGuiTop() + 22, buttonWidth, 20, text("Frame", getMenu().pump.placeFrame), this));
        this.addRenderableWidget(new IndexedButton(1, getGuiLeft() + getXSize() / 2, getGuiTop() + 22, buttonWidth, 20, text("Delete", getMenu().pump.deleteFluid), this));
    }

    private Component text(String prefix, boolean state) {
        return new TextComponent("%s %s".formatted(prefix, state ? "on" : "off"));
    }

    @Override
    public void onPress(Button button) {
        if (button instanceof IndexedButton indexedButton) {
            switch (indexedButton.getIndex()) {
                case 0 -> {
                    getMenu().pump.placeFrame = !getMenu().pump.placeFrame;
                    indexedButton.setMessage(text("Frame", getMenu().pump.placeFrame));
                    PacketHandler.sendToServer(new AdvPumpMessage(getMenu().pump));
                }
                case 1 -> {
                    getMenu().pump.deleteFluid = !getMenu().pump.deleteFluid;
                    indexedButton.setMessage(text("Delete", getMenu().pump.deleteFluid));
                    PacketHandler.sendToServer(new AdvPumpMessage(getMenu().pump));
                }
                default -> {
                }
            }
        }
    }
}
