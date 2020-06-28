package com.yogpc.qp.machines.base

import com.mojang.blaze3d.matrix.MatrixStack
import com.yogpc.qp.QuarryPlus
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.util.ResourceLocation
import net.minecraft.util.text.{ITextComponent, TextFormatting}
import net.minecraftforge.api.distmarker.{Dist, OnlyIn}

@OnlyIn(Dist.CLIENT)
class StatusGui(c: StatusContainer, inv: PlayerInventory, t: ITextComponent)
  extends net.minecraft.client.gui.screen.inventory.ContainerScreen[StatusContainer](c, inv, t) {
  val LOCATION = new ResourceLocation(QuarryPlus.modID, "textures/gui/status.png")
  xSize = 176
  ySize = 226

  override def func_230450_a_(matrix: MatrixStack, partialTicks: Float, mouseX: Int, mouseY: Int): Unit = {
    ScreenUtil.color4f()
    this.getMinecraft.getTextureManager.bindTexture(LOCATION)
    this.func_238474_b_(matrix, guiLeft, guiTop, 0, 0, xSize, ySize)
  }

  override def func_230430_a_(matrixStack: MatrixStack, mouseX: Int, mouseY: Int, partialTicks: Float): Unit = { // render
    this.func_230446_a_(matrixStack) // back ground
    super.func_230430_a_(matrixStack, mouseX, mouseY, partialTicks) // super.render
    this.func_230459_a_(matrixStack, mouseX, mouseY) // render tooltip
  }

  override def func_230451_b_(matrix: MatrixStack, mouseX: Int, mouseY: Int): Unit = {
    listContent().zipWithIndex.map { case (str, i) => str -> (i * 9 + 15) }
      .foreach { case (str, i) => this.field_230712_o_.func_238421_b_(matrix, str, 8, i.toFloat, 0x404040) }
  }

  def listContent(): Seq[String] = {
    Option(getContainer.tile).collect { case p: StatusContainer.StatusProvider => p.getStatusStrings.map(TextFormatting.getTextWithoutFormattingCodes) }.getOrElse(Nil)
  }
}
