package com.yogpc.qp.machines.quarry;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.misc.SmallCheckBox;
import com.yogpc.qp.packet.PacketHandler;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class QuarryScreen extends AbstractContainerScreen<QuarryMenu> {
    private static final ResourceLocation texture = new ResourceLocation(QuarryPlus.modID, "textures/gui/adv_pump.png");

    public QuarryScreen(QuarryMenu abstractContainerMenu, Inventory inventory, Component component) {
        super(abstractContainerMenu, inventory, component);
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        this.renderTooltip(matrices, mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack matrices, float f, int i, int j) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, texture);
        this.blit(matrices, leftPos, topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new SmallCheckBox(leftPos + 8, topPos + 20, 80, 10, 10, 10,
            Component.nullToEmpty("Remove fluids"), this.menu.quarry.quarryConfig.removeFluid(), this::onPressFluidButton));
    }

    private void onPressFluidButton(Button button) {
        var before = this.menu.quarry.quarryConfig;
        this.menu.quarry.quarryConfig = before.toggleRemoveFluid();

        PacketHandler.sendToServer(new QuarryConfigSyncMessage(this.menu.quarry));
    }
}
