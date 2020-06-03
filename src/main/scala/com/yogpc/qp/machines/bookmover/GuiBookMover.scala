package com.yogpc.qp.machines.bookmover

import com.mojang.blaze3d.systems.RenderSystem
import com.yogpc.qp.QuarryPlus
import com.yogpc.qp.machines.TranslationKeys
import net.minecraft.client.gui.screen.inventory.ContainerScreen
import net.minecraft.client.resources.I18n
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.util.ResourceLocation
import net.minecraft.util.text.ITextComponent

class GuiBookMover(c: ContainerBookMover, inv: PlayerInventory, t: ITextComponent) extends ContainerScreen[ContainerBookMover](c, inv, t) {
  val LOCATION = new ResourceLocation(QuarryPlus.modID, "textures/gui/bookmover.png")

  override def drawGuiContainerBackgroundLayer(partialTicks: Float, mouseX: Int, mouseY: Int): Unit = {
    RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F)
    this.getMinecraft.getTextureManager.bindTexture(LOCATION)
    this.blit(guiLeft, guiTop, 0, 0, xSize, ySize)
    if (container.moverIsWorking()) {
      this.blit(guiLeft + 79, guiTop + 35, xSize + 0, 14, container.getProgress * 3 / 125, 16)
    }
  }

  override def render(mouseX: Int, mouseY: Int, partialTicks: Float): Unit = {
    this.renderBackground()
    super.render(mouseX, mouseY, partialTicks)
    this.renderHoveredToolTip(mouseX, mouseY)
  }

  override def drawGuiContainerForegroundLayer(p_146979_1_ : Int, p_146979_2_ : Int): Unit = {
    val s = I18n.format(TranslationKeys.moverfrombook)
    this.font.drawString(s, (this.xSize - this.font.getStringWidth(s)).toFloat / 2, 6, 0x404040)
    this.font.drawString(I18n.format(TranslationKeys.CONTAINER_INVENTORY), 8, (this.ySize - 96 + 2).toFloat, 4210752)
  }
}
