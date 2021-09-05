package com.yogpc.qp.machines.misc;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.packet.PacketHandler;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class YSetterScreen extends AbstractContainerScreen<YSetterContainer> {
    private static final ResourceLocation LOCATION = new ResourceLocation(QuarryPlus.modID, "textures/gui/adv_pump.png");
    private static final int tp = 15;

    public YSetterScreen(YSetterContainer handler, Inventory inventory, Component title) {
        super(handler, inventory, title);
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        this.renderTooltip(matrices, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(PoseStack matrices, int mouseX, int mouseY) {
        super.renderLabels(matrices, mouseX, mouseY);
        var level = getMenu().yAccessor != null ? String.valueOf(getMenu().yAccessor.getDigMinY() + 1) : "Not supported";
        this.font.draw(matrices, level, ((float) this.imageWidth - font.width(level)) / 2, tp + 23, 0x404040);
    }

    @Override
    protected void renderBg(PoseStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, LOCATION);
        this.blit(matrices, getGuiLeft(), getGuiTop(), 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void init() {
        super.init();
        final int width = 40;
        addRenderableWidget(new Button(this.getGuiLeft() + this.imageWidth / 2 - width / 2, this.getGuiTop() + tp, width, 20, new TextComponent("+"), b -> changeDigY(true)));
        addRenderableWidget(new Button(this.getGuiLeft() + this.imageWidth / 2 - width / 2, this.getGuiTop() + tp + 33, width, 20, new TextComponent("-"), b -> changeDigY(false)));
    }

    private void changeDigY(boolean plus) {
        var accessor = getMenu().yAccessor;
        if (accessor != null) {
            var count = (plus ? 1 : -1) * (Screen.hasControlDown() ? 10 : 1);
            var topLimit = accessor.getLimitTop();
            if (count + accessor.getDigMinY() < topLimit) {
                accessor.setDigMinY(count + accessor.getDigMinY());
                PacketHandler.sendToServer(accessor.makeMessage());
            }
        }
    }
}
