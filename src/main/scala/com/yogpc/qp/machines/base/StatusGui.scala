package com.yogpc.qp.machines.base

import com.mojang.blaze3d.systems.RenderSystem
import com.yogpc.qp.QuarryPlus
import com.yogpc.qp.machines.TranslationKeys
import net.minecraft.client.resources.I18n
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

  override def drawGuiContainerBackgroundLayer(partialTicks: Float, mouseX: Int, mouseY: Int): Unit = {
    RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F)
    this.getMinecraft.getTextureManager.bindTexture(LOCATION)
    this.blit(guiLeft, guiTop, 0, 0, xSize, ySize)
  }

  override def render(mouseX: Int, mouseY: Int, partialTicks: Float): Unit = {
    this.renderBackground()
    super.render(mouseX, mouseY, partialTicks)
    this.renderHoveredToolTip(mouseX, mouseY)
  }

  override def drawGuiContainerForegroundLayer(p_146979_1_ : Int, p_146979_2_ : Int): Unit = {
    val s = getContainer.tile.getBlockState.getBlock.getNameTextComponent.getFormattedText
    this.font.drawString(s, this.xSize / 2 - this.font.getStringWidth(s) / 2, 6, 0x404040)
    this.font.drawString(I18n.format(TranslationKeys.CONTAINER_INVENTORY), 8, this.ySize - 96 + 2, 0x404040)
    listContent().zipWithIndex.map { case (str, i) => str -> (i * 9 + 15) }
      .foreach { case (str, i) => this.font.drawString(str, 8, i, 0x404040) }
  }

  def listContent(): Seq[String] = {
    Option(getContainer.tile).collect { case p: StatusContainer.StatusProvider => p.getStatusStrings.map(TextFormatting.getTextWithoutFormattingCodes) }.getOrElse(Nil)
  }
}
