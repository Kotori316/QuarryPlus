package com.yogpc.qp.machines.mini_quarry;

import com.mojang.blaze3d.systems.RenderSystem;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.TranslationKeys;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class MiniQuarryGui extends ContainerScreen<MiniQuarryContainer> {

    private static final ResourceLocation LOCATION = new ResourceLocation(QuarryPlus.modID, "textures/gui/quarry_module.png");
    private final MiniQuarryContainer container;

    public MiniQuarryGui(MiniQuarryContainer c, PlayerInventory i, ITextComponent t) {
        super(c, i, t);
        container = c;
    }

    @Override
    public void render(final int mouseX, final int mouseY, final float partialTicks) {
        this.renderBackground();
        super.render(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.getMinecraft().getTextureManager().bindTexture(LOCATION);
        this.blit(guiLeft, guiTop, 0, 0, xSize, ySize);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(final int mouseX, final int mouseY) {
        this.font.drawString(title.getFormattedText(), 8, 6, 0x404040);
        this.font.drawString(I18n.format(TranslationKeys.CONTAINER_INVENTORY), 8, this.ySize - 96 + 2, 0x404040);
    }
}
