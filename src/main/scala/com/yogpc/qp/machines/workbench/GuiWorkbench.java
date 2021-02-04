package com.yogpc.qp.machines.workbench;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.base.ScreenUtil;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class GuiWorkbench extends ContainerScreen<ContainerWorkbench> {

    private static final ResourceLocation gui = new ResourceLocation(QuarryPlus.modID, "textures/gui/workbench.png");

    public GuiWorkbench(ContainerWorkbench workbench, PlayerInventory inv, ITextComponent component) {
        super(workbench, inv, component);
        this.xSize = 176;
        this.ySize = 222;
        this.playerInventoryTitleY = this.ySize - 96 + 2; // y position of text, inventory
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, final int mouseX, final int mouseY) {
        super.drawGuiContainerForegroundLayer(matrixStack, mouseX, mouseY);
        if (getContainer().requiredEnergy.get() != 0) {
            String current = getContainer().currentEnergy.get() + "FE";
            this.font.drawString(matrixStack, String.format("%s / %d", current, getContainer().requiredEnergy.get()),
                110 - this.font.getStringWidth(current), (float) this.playerInventoryTitleY, 0x404040);
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        ScreenUtil.color4f();
        this.getMinecraft().getTextureManager().bindTexture(gui);
        this.blit(matrixStack, guiLeft, guiTop, 0, 0, xSize, ySize);
        if (container.isWorking.get() == 1) {
            blit(matrixStack, guiLeft + 8, guiTop + 78, 0, this.ySize, container.progress.get(), 4);
            int cur_recipe = 27 + container.recipeIndex.get();
            int i = (container.workContinue.get() == 1 ? 16 : 0);
            blit(matrixStack, guiLeft + 8 + cur_recipe % 9 * 18, guiTop + 90 + (cur_recipe / 9 - 3) * 18, this.xSize + i, 0, 16, 16);
        }
    }

    @Override
    public void render(MatrixStack matrixStack, final int mouseX, final int mouseY, final float partialTicks) {
        this.renderBackground(matrixStack);// back ground
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.renderHoveredTooltip(matrixStack, mouseX, mouseY); // render tooltip
    }

}
