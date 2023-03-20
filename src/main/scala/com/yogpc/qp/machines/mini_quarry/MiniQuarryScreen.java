package com.yogpc.qp.machines.mini_quarry;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.misc.IndexedButton;
import com.yogpc.qp.packet.PacketHandler;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public final class MiniQuarryScreen extends AbstractContainerScreen<MiniQuarryMenu> implements Button.OnPress {
    private static final ResourceLocation LOCATION = new ResourceLocation(QuarryPlus.modID, "textures/gui/quarry_module.png");

    public MiniQuarryScreen(MiniQuarryMenu c, Inventory inventory, Component component) {
        super(c, inventory, component);
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
        com.yogpc.qp.machines.ScreenHelper.blit(matrices, getGuiLeft(), getGuiTop(), 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(PoseStack matrices, int mouseX, int mouseY) {
        super.renderLabels(matrices, mouseX, mouseY);
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new IndexedButton(1, getGuiLeft() + 70, getGuiTop() + 50, 60, 20, Component.literal("List"), this));
    }

    @Override
    public void onPress(Button b) {
        if (b instanceof IndexedButton button) {
            if (button.id() == 1) {
                PacketHandler.sendToServer(new MiniRequestListMessage(getMenu().miniQuarry));
            }
        }
    }
}
