package com.yogpc.qp.gui;

import java.util.function.IntSupplier;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.container.ContainerQuarryModule;
import com.yogpc.qp.tile.TileAdvQuarry;
import com.yogpc.qp.tile.TileQuarry2;
import javax.annotation.Nullable;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class GuiQuarryModule extends GuiContainer {

    private static final ResourceLocation LOCATION = new ResourceLocation(QuarryPlus.modID, "textures/gui/quarry_module.png");
    private final String name;
    @Nullable
    private final IntSupplier yGetter;

    public GuiQuarryModule(ContainerQuarryModule.HasModuleInventory quarry, EntityPlayer player, String name) {
        super(new ContainerQuarryModule(quarry, player));
        this.name = name;

        IntSupplier a;
        if (quarry instanceof TileQuarry2) {
            TileQuarry2 quarry2 = (TileQuarry2) quarry;
            a = () -> GuiQuarryLevel.NQuarryY().getYLevel(quarry2);
        } else if (quarry instanceof TileAdvQuarry) {
            TileAdvQuarry advQuarry = (TileAdvQuarry) quarry;
            a = () -> GuiQuarryLevel.AdvY().getYLevel(advQuarry);
        } else {
            a = null;
        }
        this.yGetter = a;
    }

    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(LOCATION);
        this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(final int mouseX, final int mouseY) {
        this.fontRenderer.drawString(I18n.format(name), 8, 6, 0x404040);
        if (yGetter != null)
            this.fontRenderer.drawString("Y: " + yGetter.getAsInt(), 120, 6, 0x404040);
        this.fontRenderer.drawString(I18n.format(TranslationKeys.CONTAINER_INVENTORY), 8, this.ySize - 96 + 2, 0x404040);
    }
}
