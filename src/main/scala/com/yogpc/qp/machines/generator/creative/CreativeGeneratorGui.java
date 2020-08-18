package com.yogpc.qp.machines.generator.creative;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.base.ScreenUtil;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class CreativeGeneratorGui extends ContainerScreen<CreativeGeneratorContainer> {
    private static final ResourceLocation LOCATION = new ResourceLocation(QuarryPlus.modID, "textures/gui/advpump.png");

    public CreativeGeneratorGui(CreativeGeneratorContainer c, PlayerInventory i, ITextComponent t) {
        super(c, i, t);
    }

    @Override
    public void render(MatrixStack matrixStack, final int mouseX, final int mouseY, final float partialTicks) {
        this.renderBackground(matrixStack);// back ground
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.func_230459_a_(matrixStack, mouseX, mouseY); // render tooltip
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrix, float partialTicks, int x, int y) {
        ScreenUtil.color4f();
        this.getMinecraft().getTextureManager().bindTexture(LOCATION);
        this.blit(matrix, guiLeft, guiTop, 0, 0, xSize, ySize);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int x, int y) {
        super.drawGuiContainerForegroundLayer(matrixStack, x, y);
    }
}
