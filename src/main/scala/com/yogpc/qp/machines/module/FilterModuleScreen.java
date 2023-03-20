package com.yogpc.qp.machines.module;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class FilterModuleScreen extends AbstractContainerScreen<FilterModuleMenu> {
    private static final ResourceLocation LOCATION = new ResourceLocation("minecraft", "textures/gui/container/generic_54.png");
    private final int containerRows;

    public FilterModuleScreen(FilterModuleMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.containerRows = 2;
        this.imageHeight = 114 + this.containerRows * 18;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    @Override
    protected void renderBg(PoseStack pPoseStack, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, LOCATION);
        com.yogpc.qp.machines.ScreenHelper.blit(pPoseStack, getGuiLeft(), getGuiTop(), 0, 0, this.imageWidth, this.containerRows * 18 + 17);
        com.yogpc.qp.machines.ScreenHelper.blit(pPoseStack, getGuiLeft(), getGuiTop() + this.containerRows * 18 + 17, 0, 126, this.imageWidth, 96);
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        this.renderTooltip(matrices, mouseX, mouseY);
    }

}
