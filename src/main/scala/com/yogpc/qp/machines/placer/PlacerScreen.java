package com.yogpc.qp.machines.placer;

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
public class PlacerScreen extends AbstractContainerScreen<PlacerContainer> {
    private static final ResourceLocation LOCATION = new ResourceLocation(QuarryPlus.modID, "textures/gui/replacer.png");

    public PlacerScreen(PlacerContainer c, Inventory inventory, Component component) {
        super(c, inventory, component);
    }

    protected ResourceLocation textureLocation() {
        return LOCATION;
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
        RenderSystem.setShaderTexture(0, textureLocation());
        this.blit(matrices, getGuiLeft(), getGuiTop(), 0, 0, imageWidth, imageHeight);
        {
            // red = 176, 0;  start = 61, 16;
            int oneBox = 18;
            int x = getMenu().startX - 1 + (getMenu().tile.getLastPlacedIndex() % 3) * oneBox;
            int y = 16 + (getMenu().tile.getLastPlacedIndex() / 3) * oneBox;
            this.blit(matrices, getGuiLeft() + x, getGuiTop() + y, 176, 0, oneBox, oneBox);
        }
    }

    @Override
    protected void renderLabels(PoseStack matrices, int mouseX, int mouseY) {
        super.renderLabels(matrices, mouseX, mouseY);
        {
            // Mode
            PlacerTile.RedstoneMode mode = this.getMenu().tile.redstoneMode;
            String pA = mode.isAlways() ? "Always" : "Pulse";
            int x = 116;
            this.font.draw(matrices, pA, x, 6, 0x404040);
            String rs;
            if (mode.isRsOn()) rs = "RS On";
            else if (mode.isRsOff()) rs = "RS Off";
            else rs = "";
            this.font.draw(matrices, rs, x, 18, 0x404040);
            String only;
            if (mode.canBreak() && !mode.canPlace()) only = "Break Only";
            else if (mode.canPlace() && !mode.canBreak()) only = "Place Only";
            else only = "";
            this.font.draw(matrices, only, x, 30, 0x404040);
        }
    }
}
