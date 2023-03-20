package com.yogpc.qp.machines.workbench;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.yogpc.qp.QuarryPlus;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ScreenWorkbench extends AbstractContainerScreen<ContainerWorkbench> {

    private static final ResourceLocation gui = new ResourceLocation(QuarryPlus.modID, "textures/gui/workbench.png");

    public ScreenWorkbench(ContainerWorkbench workbench, Inventory inventory, Component component) {
        super(workbench, inventory, component);
        this.imageWidth = 176;
        this.imageHeight = 222;
        this.inventoryLabelY = this.imageHeight - 96 + 2; // y position of text, inventory
    }

    @Override
    protected void renderLabels(PoseStack matrices, int mouseX, int mouseY) {
        super.renderLabels(matrices, mouseX, mouseY);
        if (getMenu().tile.getMaxEnergyStored() > 5) {
            String current = getMenu().tile.getEnergyStored() + "FE";
            this.font.draw(matrices, String.format("%s/%d", current, getMenu().tile.getMaxEnergyStored()),
                120 - this.font.width(current), this.inventoryLabelY, 0x404040);
        }
    }

    @Override
    protected void renderBg(PoseStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, gui);
        com.yogpc.qp.machines.ScreenHelper.blit(matrices, getGuiLeft(), getGuiTop(), 0, 0, imageWidth, imageHeight);
        if (getMenu().isWorking.get() == 1) {
            blit(matrices, getGuiLeft() + 8, getGuiTop() + 78, 0, this.imageHeight, getMenu().progress.get(), 4);
            int cur_recipe = 27 + getMenu().recipeIndex.get();
            int i = (getMenu().workContinue.get() == 1 ? 16 : 0);
            blit(matrices, getGuiLeft() + 8 + cur_recipe % 9 * 18, getGuiTop() + 90 + (cur_recipe / 9 - 3) * 18, this.imageWidth + i, 0, 16, 16);
        }
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);
        super.render(matrices, mouseX, mouseY, delta);
        this.renderTooltip(matrices, mouseX, mouseY);
    }

}
