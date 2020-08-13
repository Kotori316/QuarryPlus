package com.yogpc.qp.machines.advpump

import com.mojang.blaze3d.matrix.MatrixStack
import com.yogpc.qp.QuarryPlus
import com.yogpc.qp.machines.base.{IHandleButton, ScreenUtil}
import com.yogpc.qp.packet.PacketHandler
import com.yogpc.qp.packet.advpump.AdvPumpChangeMessage
import net.minecraft.client.gui.screen.inventory.ContainerScreen
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.util.ResourceLocation
import net.minecraft.util.text.{ITextComponent, StringTextComponent, TranslationTextComponent}
import net.minecraftforge.api.distmarker.{Dist, OnlyIn}

@OnlyIn(Dist.CLIENT)
class GuiAdvPump(c: ContainerAdvPump, i: PlayerInventory, t: ITextComponent) extends ContainerScreen[ContainerAdvPump](c, i, t) with IHandleButton {
  val tile: TileAdvPump = getContainer.tile
  val LOCATION = new ResourceLocation(QuarryPlus.modID, "textures/gui/advpump.png")

  override def render(matrixStack: MatrixStack, mouseX: Int, mouseY: Int, partialTicks: Float): Unit = { // render
    this.renderBackground(matrixStack) // back ground
    super.render(matrixStack, mouseX, mouseY, partialTicks) // super.render
    this.func_230459_a_(matrixStack, mouseX, mouseY) // render tooltip
  }

  override def init(): Unit = {
    super.init() // init
    val buttonWidth = 80
    addButton(new IHandleButton.Button(0, guiLeft + getXSize / 2 - buttonWidth, guiTop + 22, buttonWidth, 20, frameText(tile.placeFrame), this))
    val startButton = addButton(new IHandleButton.Button(1, guiLeft + getXSize / 2 - 60, guiTop + 45, 120, 20, new StringTextComponent("Start"), this))
    val deleteButton = new IHandleButton.Button(2, guiLeft + getXSize / 2, guiTop + 22, buttonWidth, 20, deleteText(tile.delete), this)
    addButton(deleteButton)
    addButton(new IHandleButton.Button(3, guiLeft + getXSize / 2 + 20, guiTop + 65, 60, 15, new StringTextComponent("Module"), this))
    startButton.active = !tile.isWorking
    val toolTipDeleteButton = java.util.Collections.singletonList[ITextComponent](new TranslationTextComponent("quarryplus.tooltip.advpump.gui_delete"))
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

  override def drawGuiContainerForegroundLayer(matrix: MatrixStack, mouseX: Int, mouseY: Int): Unit = {
    super.drawGuiContainerForegroundLayer(matrix, mouseX, mouseY)
//    this.buttons.asScala.filter(_.func_230449_g_()) //hovered?
//      .foreach(_.renderToolTip(matrix, mouseX - this.guiLeft, mouseY - this.guiTop))
  }

  override def drawGuiContainerBackgroundLayer(matrix: MatrixStack, partialTicks: Float, mouseX: Int, mouseY: Int): Unit = {
    ScreenUtil.color4f()
    this.getMinecraft.getTextureManager.bindTexture(LOCATION)
    this.blit(matrix, guiLeft, guiTop, 0, 0, xSize, ySize)
  }

  def frameText(boolean: Boolean): TranslationTextComponent = {
    new TranslationTextComponent("quarryplus.gui.advpump.frame_" + boolean)
  }

  def deleteText(boolean: Boolean): TranslationTextComponent = new TranslationTextComponent("quarryplus.gui.advpump.delete_" + boolean)
}
