package com.yogpc.qp.machines.advpump

import com.yogpc.qp.QuarryPlus
import com.yogpc.qp.machines.TranslationKeys
import com.yogpc.qp.machines.base.IHandleButton
import com.yogpc.qp.packet.PacketHandler
import com.yogpc.qp.packet.advpump.AdvPumpChangeMessage
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.resources.I18n
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ResourceLocation
import net.minecraftforge.api.distmarker.{Dist, OnlyIn}

@OnlyIn(Dist.CLIENT)
class GuiAdvPump(tile: TileAdvPump, player: EntityPlayer) extends GuiContainer(new ContainerAdvPump(tile, player)) with IHandleButton {

  val LOCATION = new ResourceLocation(QuarryPlus.modID, "textures/gui/advpump.png")

  override def render(mouseX: Int, mouseY: Int, partialTicks: Float): Unit = {
    this.drawDefaultBackground()
    super.render(mouseX, mouseY, partialTicks)
    this.renderHoveredToolTip(mouseX, mouseY)
  }

  override def initGui(): Unit = {
    super.initGui()
    addButton(new IHandleButton.Button(0, guiLeft + 12, guiTop + 22, 120, 20, "PlaceFrame = " + tile.placeFrame, this))
    addButton(new IHandleButton.Button(1, guiLeft + 12, guiTop + 47, 120, 20, "Start", this))
    buttons.get(1).enabled = !tile.isWorking
  }

  override def actionPerformed(button: GuiButton): Unit = {
    button.id match {
      case 0 => tile.placeFrame = !tile.placeFrame
        this.buttons.get(0).displayString = "PlaceFrame = " + tile.placeFrame
        PacketHandler.sendToServer(AdvPumpChangeMessage.create(tile, AdvPumpChangeMessage.ToStart.UNCHANGED))
      case 1 => this.buttons.get(button.id).enabled = false
        PacketHandler.sendToServer(AdvPumpChangeMessage.create(tile, AdvPumpChangeMessage.ToStart.START))
      case _ => QuarryPlus.LOGGER.error("AdvPump undefined button")
    }
  }

  override def drawGuiContainerForegroundLayer(mouseX: Int, mouseY: Int): Unit = {
    val s: String = I18n.format(TranslationKeys.advpump)
    this.fontRenderer.drawString(s, this.xSize / 2 - this.fontRenderer.getStringWidth(s) / 2, 6, 0x404040)
    this.fontRenderer.drawString(I18n.format(TranslationKeys.CONTAINER_INVENTORY), 8, this.ySize - 96 + 2, 0x404040)
  }

  override def drawGuiContainerBackgroundLayer(partialTicks: Float, mouseX: Int, mouseY: Int): Unit = {
    GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F)
    this.mc.getTextureManager.bindTexture(LOCATION)
    this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize)
  }
}
