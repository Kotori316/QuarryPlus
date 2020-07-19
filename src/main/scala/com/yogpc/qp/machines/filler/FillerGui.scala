package com.yogpc.qp.machines.filler

import com.mojang.blaze3d.matrix.MatrixStack
import com.yogpc.qp.QuarryPlus
import com.yogpc.qp.machines.base.ScreenUtil
import net.minecraft.client.gui.screen.inventory.ContainerScreen
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.util.ResourceLocation
import net.minecraft.util.text.ITextComponent

class FillerGui(c: FillerContainer, inv: PlayerInventory, t: ITextComponent) extends ContainerScreen[FillerContainer](c, inv, t) {
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
}
