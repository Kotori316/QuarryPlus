package com.yogpc.qp.gui

import com.yogpc.qp.QuarryPlus
import com.yogpc.qp.container.ContainerBookMover
import com.yogpc.qp.tile.TileBookMover
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.resources.I18n
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ResourceLocation

class GuiBookMover(mover: TileBookMover, player: EntityPlayer) extends GuiContainer(new ContainerBookMover(mover, player)) {
    val LOCATION = new ResourceLocation(QuarryPlus.modID, "textures/gui/bookmover.png")

    override def drawGuiContainerBackgroundLayer(partialTicks: Float, mouseX: Int, mouseY: Int): Unit = {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F)
        this.mc.getTextureManager.bindTexture(LOCATION)
        this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize)
    }

    override def drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float): Unit = {
        //        this.drawDefaultBackground()
        super.drawScreen(mouseX, mouseY, partialTicks)
        //        this.renderHoveredToolTip(mouseX, mouseY)
    }

    override def drawGuiContainerForegroundLayer(p_146979_1_ : Int, p_146979_2_ : Int) {
        val s = I18n.format(TranslationKeys.moverfrombook)
        this.fontRendererObj.drawString(s, this.xSize / 2 - this.fontRendererObj.getStringWidth(s) / 2, 6, 0x404040)
        this.fontRendererObj.drawString(I18n.format(TranslationKeys.CONTAINER_INVENTORY), 8, this.ySize - 96 + 2, 4210752)
    }
}
