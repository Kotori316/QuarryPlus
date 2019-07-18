package com.yogpc.qp.gui

import com.yogpc.qp.QuarryPlus
import com.yogpc.qp.container.ContainerQuarryLevel
import com.yogpc.qp.gui.GuiQuarryLevel.YLevel
import com.yogpc.qp.packet.PacketHandler
import com.yogpc.qp.packet.advquarry.AdvLevelMessage
import com.yogpc.qp.packet.quarry.{Level2Message, LevelMessage}
import com.yogpc.qp.tile.{HasInv, TileAdvQuarry, TileBasic, TileQuarry, TileQuarry2}
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.resources.I18n
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.ResourceLocation

class GuiQuarryLevel[T <: TileEntity with HasInv](private[this] val tile: T, player: EntityPlayer)
                                                 (implicit lA: YLevel[T], func: T => _ <: LevelMessage)
  extends GuiContainer(new ContainerQuarryLevel(tile, player)) {

  val LOCATION = new ResourceLocation(QuarryPlus.modID, "textures/gui/advpump.png")

  val tp = 15

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
    val s: String = I18n.format(tile.getName)
    this.fontRenderer.drawString(s, this.xSize / 2 - this.fontRenderer.getStringWidth(s) / 2, 6, 0x404040)
    this.fontRenderer.drawString(I18n.format(TranslationKeys.CONTAINER_INVENTORY), 8, this.ySize - 96 + 2, 0x404040)
    this.fontRenderer.drawString(lA.getYLevel(tile).toString, this.xSize / 2 - this.fontRenderer.getStringWidth(lA.getYLevel(tile).toString) / 2, tp + 23, 0x404040)
  }

  override def actionPerformed(button: GuiButton): Unit = {
    super.actionPerformed(button)
    val di = if (button.id % 2 == 0) 1 else -1
    val yMin = tile match {
      case quarry: TileQuarry => quarry.yMin
      case _ => tile.getPos.getY
    }
    if (yMin > lA.getYLevel(tile) + di)
      lA.add(tile, di)
  }

  override def onGuiClosed(): Unit = {
    super.onGuiClosed()
    PacketHandler.sendToServer(func(tile))
  }
}

object GuiQuarryLevel {

  trait YLevel[-T] {
    def setYLevel(t: T, yLevel: Int): Unit

    def getYLevel(t: T): Int

    def add(t: T, di: Int): Unit = {
      setYLevel(t, getYLevel(t) + di)
    }
  }

  implicit val BasicY: YLevel[TileBasic] = new YLevel[TileBasic] {
    override def setYLevel(t: TileBasic, yLevel: Int): Unit = t.yLevel = yLevel

    override def getYLevel(t: TileBasic) = t.yLevel
  }

  implicit val AdvY: YLevel[TileAdvQuarry] = new YLevel[TileAdvQuarry] {
    override def setYLevel(t: TileAdvQuarry, yLevel: Int): Unit = t.yLevel = yLevel

    override def getYLevel(t: TileAdvQuarry) = t.yLevel
  }
  implicit val NQuarryY: YLevel[TileQuarry2] = new YLevel[TileQuarry2] {
    override def setYLevel(t: TileQuarry2, yLevel: Int): Unit = t.yLevel = yLevel

    override def getYLevel(t: TileQuarry2) = t.yLevel
  }

  implicit val basicMessage: TileBasic => LevelMessage = LevelMessage.create
  implicit val advMessage: TileAdvQuarry => AdvLevelMessage = AdvLevelMessage.create
  implicit val quarryMessage:TileQuarry2 => Level2Message = Level2Message.create
}