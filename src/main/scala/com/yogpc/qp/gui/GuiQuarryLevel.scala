package com.yogpc.qp.gui

import com.yogpc.qp.QuarryPlus
import com.yogpc.qp.container.ContainerQuarryLevel
import com.yogpc.qp.packet.PacketHandler
import com.yogpc.qp.packet.quarry.LevelMessage
import com.yogpc.qp.tile.TileQuarry
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.resources.I18n
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ResourceLocation

class GuiQuarryLevel(private[this] val tile: TileQuarry, player: EntityPlayer) extends GuiContainer(new ContainerQuarryLevel(tile, player)) {

  val LOCATION = new ResourceLocation(QuarryPlus.modID, "textures/gui/advpump.png")

  val tp = 15
  //7,15 to 168,70 box : 162, 56

  override def drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float): Unit = {
    this.drawDefaultBackground()
    super.drawScreen(mouseX, mouseY, partialTicks)
    this.renderHoveredToolTip(mouseX, mouseY)
  }

  override def drawGuiContainerBackgroundLayer(partialTicks: Float, mouseX: Int, mouseY: Int): Unit = {
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F)
    this.mc.getTextureManager.bindTexture(LOCATION)
    this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize)
  }

  override def initGui(): Unit = {
    super.initGui()
    val width = 40
    this.buttonList.add(new GuiButton(0, guiLeft + this.xSize / 2 - width / 2, guiTop + tp, width, 20, "+"))
    this.buttonList.add(new GuiButton(1, guiLeft + this.xSize / 2 - width / 2, guiTop + tp + 33, width, 20, "-"))
  }

  override def drawGuiContainerForegroundLayer(mouseX: Int, mouseY: Int): Unit = {
    val s: String = I18n.format(TranslationKeys.quarry)
    this.fontRenderer.drawString(s, this.xSize / 2 - this.fontRenderer.getStringWidth(s) / 2, 6, 0x404040)
    this.fontRenderer.drawString(I18n.format(TranslationKeys.CONTAINER_INVENTORY), 8, this.ySize - 96 + 2, 0x404040)
    this.fontRenderer.drawString(tile.yLevel.toString, this.xSize / 2 - this.fontRenderer.getStringWidth(tile.yLevel.toString) / 2, tp + 23, 0x404040)
  }

  override def actionPerformed(button: GuiButton): Unit = {
    super.actionPerformed(button)
    val di = if (button.id % 2 == 0) 1 else -1
    if (tile.yMin > tile.yLevel + di)
      tile.yLevel += di
  }

  override def onGuiClosed(): Unit = {
    super.onGuiClosed()
    PacketHandler.sendToServer(LevelMessage.create(tile))
  }
}
