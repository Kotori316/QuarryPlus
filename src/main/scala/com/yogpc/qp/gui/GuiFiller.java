package com.yogpc.qp.gui;

import java.util.concurrent.atomic.AtomicInteger;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.container.ContainerFiller;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.packet.filler.FillerActionMessage;
import com.yogpc.qp.tile.TileFiller;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiFiller extends GuiContainer {
    private static final ResourceLocation LOCATION = new ResourceLocation(QuarryPlus.modID, "textures/gui/filler.png");
    private final TileFiller tile;

    public GuiFiller(TileFiller tile, EntityPlayer player) {
        super(new ContainerFiller(tile, player));
        this.tile = tile;
        this.xSize = 176;
        this.ySize = 222;
    }

    @Override
    public void initGui() {
        super.initGui();
        AtomicInteger id = new AtomicInteger(0);
        addButton(new GuiButton(id.getAndIncrement(), this.xSize + 50, this.ySize - 96 + 7, 60, 14, "Modules"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 0: // Module
                PacketHandler.sendToServer(FillerActionMessage.create(this.tile, FillerActionMessage.Actions.MODULE_INV));
                break;
            default:
        }
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
        this.fontRenderer.drawString(I18n.format(TranslationKeys.filler), 8, 6, 0x404040);
        this.fontRenderer.drawString(I18n.format(TranslationKeys.CONTAINER_INVENTORY), 8, this.ySize - 96 + 2, 0x404040);
    }
}
