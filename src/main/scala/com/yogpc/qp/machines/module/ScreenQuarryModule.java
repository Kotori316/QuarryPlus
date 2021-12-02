package com.yogpc.qp.machines.module;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.misc.YAccessor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.Nullable;

public class ScreenQuarryModule extends AbstractContainerScreen<ContainerQuarryModule> {

    private static final ResourceLocation LOCATION = new ResourceLocation(QuarryPlus.modID, "textures/gui/quarry_module.png");
    @Nullable
    private final YAccessor yGetter;

    public ScreenQuarryModule(ContainerQuarryModule c, Inventory inventory, Component component) {
        super(c, inventory, component);
        this.yGetter = YAccessor.get(c.blockEntity);
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
    protected void renderLabels(PoseStack matrices, int mouseX, int mouseY) {
        super.renderLabels(matrices, mouseX, mouseY);
        this.font.draw(matrices, "Modules", this.titleLabelX, this.titleLabelY + 10, 0x404040);
        if (yGetter != null) {
            this.font.draw(matrices, "Y: " + (yGetter.getDigMinY() + 1), 120, this.titleLabelY, 0x404040);
        }
    }
}
