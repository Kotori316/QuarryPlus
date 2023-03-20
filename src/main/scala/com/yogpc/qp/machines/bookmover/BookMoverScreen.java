package com.yogpc.qp.machines.bookmover;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.yogpc.qp.QuarryPlus;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class BookMoverScreen extends AbstractContainerScreen<BookMoverMenu> {
    private static final ResourceLocation LOCATION = new ResourceLocation(QuarryPlus.modID, "textures/gui/book_mover.png");

    public BookMoverScreen(BookMoverMenu c, Inventory inventory, Component component) {
        super(c, inventory, component);
    }

    @Override
    protected void renderBg(PoseStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, LOCATION);
        com.yogpc.qp.machines.ScreenHelper.blit(matrices, getGuiLeft(), getGuiTop(), 0, 0, imageWidth, imageHeight);
        if (getMenu().moverIsWorking()) {
            com.yogpc.qp.machines.ScreenHelper.blit(matrices, getGuiLeft() + 79, getGuiTop() + 35, imageWidth, 14, getMenu().getProgress() * 3 / 125, 16);
        }
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        this.renderTooltip(matrices, mouseX, mouseY);
    }
}
