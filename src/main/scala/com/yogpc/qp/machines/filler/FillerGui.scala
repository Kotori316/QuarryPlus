package com.yogpc.qp.machines.filler

import com.mojang.blaze3d.matrix.MatrixStack
import com.yogpc.qp.QuarryPlus
import com.yogpc.qp.machines.base.{IHandleButton, ScreenUtil}
import com.yogpc.qp.packet.PacketHandler
import com.yogpc.qp.packet.filler.FillerModuleMessage
import net.minecraft.client.gui.screen.inventory.ContainerScreen
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.util.ResourceLocation
import net.minecraft.util.text.{ITextComponent, StringTextComponent}

class FillerGui(c: FillerContainer, inv: PlayerInventory, t: ITextComponent) extends ContainerScreen[FillerContainer](c, inv, t) with IHandleButton {
  val LOCATION = new ResourceLocation(QuarryPlus.modID, "textures/gui/filler.png")
  this.xSize = 176
  this.ySize = 222
  this.field_238745_s_ = this.ySize - 96 + 2; // y position of text, inventory

  override def func_230450_a_(matrixStack: MatrixStack, partialTicks: Float, mouseX: Int, mouseY: Int): Unit = {
    ScreenUtil.color4f()
    this.getMinecraft.getTextureManager.bindTexture(LOCATION)
    this.func_238474_b_(matrixStack, guiLeft, guiTop, 0, 0, xSize, ySize)
  }

  override def func_230430_a_(matrixStack: MatrixStack, mouseX: Int, mouseY: Int, partialTicks: Float): Unit = { // render
    this.func_230446_a_(matrixStack) // back ground
    super.func_230430_a_(matrixStack, mouseX, mouseY, partialTicks) // super.render
    this.func_230459_a_(matrixStack, mouseX, mouseY) // render tooltip
  }

  override def func_231160_c_(): Unit = {
    super.func_231160_c_()
    val buttonWidth = 60
    func_230480_a_(new IHandleButton.Button(1, guiLeft + getXSize / 2 + 20, guiTop + 6, buttonWidth, 20, new StringTextComponent("Module"), this))
  }

  override def actionPerformed(button: IHandleButton.Button): Unit = button.id match {
    case 1 => //Module
      PacketHandler.sendToServer(FillerModuleMessage.create(getContainer.tile))
    case _ =>
  }
}
