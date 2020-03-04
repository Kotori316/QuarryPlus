package com.yogpc.qp.machines.pb;

import com.mojang.blaze3d.systems.RenderSystem;
import com.yogpc.qp.QuarryPlus;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class PlacerGui extends ContainerScreen<PlacerContainer> {
    private static final ResourceLocation LOCATION = new ResourceLocation(QuarryPlus.modID, "textures/gui/replacer.png");

    public PlacerGui(PlacerContainer c, PlayerInventory inv, ITextComponent t) {
        super(c, inv, t);
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
        {
            // red = 176, 0;  start = 61, 16;
            int oneBox = 18;
            int x = 61 + (container.tile.getLastPlacedIndex() % 3) * oneBox;
            int y = 16 + (container.tile.getLastPlacedIndex() / 3) * oneBox;
            this.blit(guiLeft + x, guiTop + y, 176, 0, oneBox, oneBox);
        }
    }

    @Override
    protected void drawGuiContainerForegroundLayer(final int mouseX, final int mouseY) {
        this.font.drawString(title.getFormattedText(), 8, 6, 0x404040);
        this.font.drawString(this.playerInventory.getDisplayName().getFormattedText(), 8, this.ySize - 96 + 2, 0x404040);
        {
            // Mode
            PlacerTile.RedstoneMode mode = this.container.tile.redstoneMode;
            String pA = mode.isAlways() ? "Always" : "Pulse";
            int x = 116;
            this.font.drawString(pA, x, 6, 0x404040);
            String rs;
            if (mode.isRsOn()) rs = "RS On";
            else if (mode.isRsOff()) rs = "RS Off";
            else rs = "";
            this.font.drawString(rs, x, 18, 0x404040);
            String only;
            if (mode.canBreak() && !mode.canPlace()) only = "Break Only";
            else if (mode.canPlace() && !mode.canBreak()) only = "Place Only";
            else only = "";
            this.font.drawString(only, x, 30, 0x404040);
        }
    }
}
