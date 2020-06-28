package com.yogpc.qp.machines.bookmover

import com.mojang.blaze3d.matrix.MatrixStack
import com.yogpc.qp.QuarryPlus
import com.yogpc.qp.machines.base.ScreenUtil
import net.minecraft.client.gui.screen.inventory.ContainerScreen
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.util.ResourceLocation
import net.minecraft.util.text.ITextComponent

class GuiBookMover(c: ContainerBookMover, inv: PlayerInventory, t: ITextComponent) extends ContainerScreen[ContainerBookMover](c, inv, t) {
  val LOCATION = new ResourceLocation(QuarryPlus.modID, "textures/gui/bookmover.png")

  override def func_230450_a_(matrixStack: MatrixStack, partialTicks: Float, mouseX: Int, mouseY: Int): Unit = {
    ScreenUtil.color4f()
    this.getMinecraft.getTextureManager.bindTexture(LOCATION)
    this.func_238474_b_(matrixStack, guiLeft, guiTop, 0, 0, xSize, ySize)
    if (container.moverIsWorking()) {
      this.func_238474_b_(matrixStack, guiLeft + 79, guiTop + 35, xSize + 0, 14, container.getProgress * 3 / 125, 16)
    }
  }

  override def func_230430_a_(matrixStack: MatrixStack, mouseX: Int, mouseY: Int, partialTicks: Float): Unit = { // render
    this.func_230446_a_(matrixStack) // back ground
    super.func_230430_a_(matrixStack, mouseX, mouseY, partialTicks) // super.render
    this.func_230459_a_(matrixStack, mouseX, mouseY) // render tooltip
  }
}
