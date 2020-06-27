package com.yogpc.qp.machines.quarry;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.yogpc.qp.QuarryPlus;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiSolidQuarry extends ContainerScreen<ContainerSolidQuarry> {

    private static final ResourceLocation LOCATION = new ResourceLocation(QuarryPlus.modID, "textures/gui/solidquarry.png");

    public GuiSolidQuarry(ContainerSolidQuarry c, PlayerInventory i, ITextComponent t) {
        super(c, i, t);
    }

    @Override
    public void func_230430_a_(MatrixStack matrixStack, final int mouseX, final int mouseY, final float partialTicks) {
        this.func_230446_a_(matrixStack);// back ground
        super.func_230430_a_(matrixStack, mouseX, mouseY, partialTicks);
        this.func_230459_a_(matrixStack, mouseX, mouseY); // render tooltip
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void func_230450_a_(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.getMinecraft().getTextureManager().bindTexture(LOCATION);
        this.func_238474_b_(matrixStack, guiLeft, guiTop, 0, 0, xSize, ySize);
    }

    @Override
    protected void func_230451_b_(MatrixStack matrixStack, final int mouseX, final int mouseY) {
        super.func_230451_b_(matrixStack, mouseX, mouseY);
        this.field_230712_o_.func_238421_b_(matrixStack, "Fuel: " + getContainer().fuelCount.get(), 110, 6, 0x404040);
    }
}
