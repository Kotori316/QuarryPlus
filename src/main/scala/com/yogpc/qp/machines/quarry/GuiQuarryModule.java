package com.yogpc.qp.machines.quarry;

import java.util.function.IntSupplier;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.advquarry.TileAdvQuarry;
import com.yogpc.qp.machines.base.ScreenUtil;
import com.yogpc.qp.machines.item.GuiQuarryLevel;
import javax.annotation.Nullable;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class GuiQuarryModule extends ContainerScreen<ContainerQuarryModule> {

    private static final ResourceLocation LOCATION = new ResourceLocation(QuarryPlus.modID, "textures/gui/quarry_module.png");
    @Nullable
    private final IntSupplier yGetter;

    public GuiQuarryModule(ContainerQuarryModule c, PlayerInventory i, ITextComponent t) {
        super(c, i, t);

        IntSupplier a;
        if (c.inventory instanceof TileQuarry2) {
            TileQuarry2 quarry2 = (TileQuarry2) c.inventory;
            a = () -> GuiQuarryLevel.NQuarryY().getYLevel(quarry2);
        } else if (c.inventory instanceof TileAdvQuarry) {
            TileAdvQuarry advQuarry = (TileAdvQuarry) c.inventory;
            a = () -> GuiQuarryLevel.AdvY().getYLevel(advQuarry);
        } else {
            a = null;
        }
        this.yGetter = a;
    }

    @Override
    public void render(MatrixStack matrixStack, final int mouseX, final int mouseY, final float partialTicks) {
        this.renderBackground(matrixStack);// back ground
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        this.func_230459_a_(matrixStack, mouseX, mouseY); // render tooltip
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        ScreenUtil.color4f();
        this.getMinecraft().getTextureManager().bindTexture(LOCATION);
        this.blit(matrixStack, guiLeft, guiTop, 0, 0, xSize, ySize);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, final int mouseX, final int mouseY) {
        super.drawGuiContainerForegroundLayer(matrixStack, mouseX, mouseY);
        if (yGetter != null) {
            this.font.drawString(matrixStack, "Y: " + yGetter.getAsInt(), 120, 6, 0x404040);
        }
    }
}
