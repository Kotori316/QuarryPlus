package com.yogpc.qp.machines.advpump

import com.mojang.blaze3d.systems.RenderSystem
import com.yogpc.qp.QuarryPlus
import com.yogpc.qp.machines.TranslationKeys
import com.yogpc.qp.machines.base.IHandleButton
import com.yogpc.qp.packet.PacketHandler
import com.yogpc.qp.packet.advpump.AdvPumpChangeMessage
import net.minecraft.client.gui.screen.inventory.ContainerScreen
import net.minecraft.client.resources.I18n
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.util.ResourceLocation
import net.minecraft.util.text.ITextComponent
import net.minecraftforge.api.distmarker.{Dist, OnlyIn}

import scala.jdk.CollectionConverters._

@OnlyIn(Dist.CLIENT)
class GuiAdvPump(c: ContainerAdvPump, i: PlayerInventory, t: ITextComponent) extends ContainerScreen[ContainerAdvPump](c, i, t) with IHandleButton {
  val tile: TileAdvPump = getContainer.tile
  val LOCATION = new ResourceLocation(QuarryPlus.modID, "textures/gui/advpump.png")

  override def render(mouseX: Int, mouseY: Int, partialTicks: Float): Unit = {
    this.renderBackground()
    super.render(mouseX, mouseY, partialTicks)
    this.renderHoveredToolTip(mouseX, mouseY)
  }

  override def init(): Unit = {
    super.init()
    val buttonWidth = 80
    addButton(new IHandleButton.Button(0, guiLeft + getXSize / 2 - buttonWidth, guiTop + 22, buttonWidth, 20, frameText(tile.placeFrame), this))
    addButton(new IHandleButton.Button(1, guiLeft + getXSize / 2 - 60, guiTop + 45, 120, 20, "Start", this))
    val deleteButton = new IHandleButton.Button(2, guiLeft + getXSize / 2, guiTop + 22, buttonWidth, 20, deleteText(tile.delete), this)
    addButton(deleteButton)
    addButton(new IHandleButton.Button(3, guiLeft + getXSize / 2 + 20, guiTop + 65, 60, 15, "Module", this))
    buttons.get(1).active = !tile.isWorking
    val toolTipDeleteButton = java.util.Arrays.asList(I18n.format("quarryplus.tooltip.advpump.gui_delete").split("\n"): _*)
    deleteButton.setToolTip(() => toolTipDeleteButton, this)
  }

  override def actionPerformed(button: IHandleButton.Button): Unit = {
    button.id match {
      case 0 => tile.placeFrame = !tile.placeFrame
        button.setMessage(frameText(tile.placeFrame))
        PacketHandler.sendToServer(AdvPumpChangeMessage.create(tile, AdvPumpChangeMessage.ToStart.UNCHANGED))
      case 1 => this.buttons.get(button.id).active = false
        PacketHandler.sendToServer(AdvPumpChangeMessage.create(tile, AdvPumpChangeMessage.ToStart.START))
      case 2 => tile.toggleDelete()
        button.setMessage(deleteText(tile.delete))
        PacketHandler.sendToServer(AdvPumpChangeMessage.create(tile, AdvPumpChangeMessage.ToStart.UNCHANGED))
      case 3 =>
        PacketHandler.sendToServer(AdvPumpChangeMessage.create(tile, AdvPumpChangeMessage.ToStart.MODULE_INV))
      case _ => QuarryPlus.LOGGER.error("AdvPump undefined button")
    }
  }

  override def drawGuiContainerForegroundLayer(mouseX: Int, mouseY: Int): Unit = {
    val s: String = I18n.format(TranslationKeys.advpump)
    this.font.drawString(s, (this.xSize - this.font.getStringWidth(s)).toFloat / 2, 6, 0x404040)
    this.font.drawString(I18n.format(TranslationKeys.CONTAINER_INVENTORY), 8, (this.ySize - 96 + 2).toFloat, 0x404040)

    this.buttons.asScala.filter(_.isHovered)
      .foreach(_.renderToolTip(mouseX - this.guiLeft, mouseY - this.guiTop))
  }

  override def drawGuiContainerBackgroundLayer(partialTicks: Float, mouseX: Int, mouseY: Int): Unit = {
    RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F)
    this.getMinecraft.getTextureManager.bindTexture(LOCATION)
    this.blit(guiLeft, guiTop, 0, 0, xSize, ySize)
  }

  def frameText(boolean: Boolean): String = {
    I18n.format("quarryplus.gui.advpump.frame_" + boolean)
  }

  def deleteText(boolean: Boolean): String = I18n.format("quarryplus.gui.advpump.delete_" + boolean)
}
