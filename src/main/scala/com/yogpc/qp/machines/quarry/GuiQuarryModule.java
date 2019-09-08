package com.yogpc.qp.machines.quarry;

import java.util.function.IntSupplier;

import com.mojang.blaze3d.platform.GlStateManager;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.TranslationKeys;
import com.yogpc.qp.machines.advquarry.TileAdvQuarry;
import com.yogpc.qp.machines.item.GuiQuarryLevel;
import javax.annotation.Nullable;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.resources.I18n;
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
    public void render(final int mouseX, final int mouseY, final float partialTicks) {
        this.renderBackground();
        super.render(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.getMinecraft().getTextureManager().bindTexture(LOCATION);
        this.blit(guiLeft, guiTop, 0, 0, xSize, ySize);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(final int mouseX, final int mouseY) {
        this.font.drawString(title.getFormattedText(), 8, 6, 0x404040);
        if (yGetter != null)
            this.font.drawString("Y: " + yGetter.getAsInt(), 120, 6, 0x404040);
        this.font.drawString(I18n.format(TranslationKeys.CONTAINER_INVENTORY), 8, this.ySize - 96 + 2, 0x404040);
    }
}
