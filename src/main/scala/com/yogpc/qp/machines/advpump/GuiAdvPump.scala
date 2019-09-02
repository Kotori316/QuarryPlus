package com.yogpc.qp.machines.advpump

import com.mojang.blaze3d.platform.GlStateManager
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

@OnlyIn(Dist.CLIENT)
class GuiAdvPump(c: ContainerAdvPump, i: PlayerInventory, t: ITextComponent) extends ContainerScreen[ContainerAdvPump](c, i, t) with IHandleButton {
  val tile = getContainer.tile
  val LOCATION = new ResourceLocation(QuarryPlus.modID, "textures/gui/advpump.png")

  override def render(mouseX: Int, mouseY: Int, partialTicks: Float): Unit = {
    this.renderBackground()
    super.render(mouseX, mouseY, partialTicks)
    this.renderHoveredToolTip(mouseX, mouseY)
  }

  override def init(): Unit = {
    super.init()
    addButton(new IHandleButton.Button(0, guiLeft + 12, guiTop + 22, 120, 20, "PlaceFrame = " + tile.placeFrame, this))
    addButton(new IHandleButton.Button(1, guiLeft + 12, guiTop + 47, 120, 20, "Start", this))
    buttons.get(1).active = !tile.isWorking
  }

  override def actionPerformed(button: IHandleButton.Button): Unit = {
    button.id match {
      case 0 => tile.placeFrame = !tile.placeFrame
        this.buttons.get(0).setMessage("PlaceFrame = " + tile.placeFrame)
        PacketHandler.sendToServer(AdvPumpChangeMessage.create(tile, AdvPumpChangeMessage.ToStart.UNCHANGED))
      case 1 => this.buttons.get(button.id).active = false
        PacketHandler.sendToServer(AdvPumpChangeMessage.create(tile, AdvPumpChangeMessage.ToStart.START))
      case _ => QuarryPlus.LOGGER.error("AdvPump undefined button")
    }
  }

  override def drawGuiContainerForegroundLayer(mouseX: Int, mouseY: Int): Unit = {
    val s: String = I18n.format(TranslationKeys.advpump)
    this.font.drawString(s, this.xSize / 2 - this.font.getStringWidth(s) / 2, 6, 0x404040)
    this.font.drawString(I18n.format(TranslationKeys.CONTAINER_INVENTORY), 8, this.ySize - 96 + 2, 0x404040)
  }

  override def drawGuiContainerBackgroundLayer(partialTicks: Float, mouseX: Int, mouseY: Int): Unit = {
    GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F)
    this.getMinecraft.getTextureManager.bindTexture(LOCATION)
    this.blit(guiLeft, guiTop, 0, 0, xSize, ySize)
  }
}
