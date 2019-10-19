package com.yogpc.qp.gui

import com.yogpc.qp.QuarryPlus
import com.yogpc.qp.container.StatusContainer
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.resources.I18n
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.TextFormatting
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

@SideOnly(Side.CLIENT)
class StatusGui(player: EntityPlayer, pos: BlockPos)
  extends GuiContainer(new StatusContainer(0, player, pos)) {
  val LOCATION = new ResourceLocation(QuarryPlus.modID, "textures/gui/status.png")
  xSize = 176
  ySize = 226

  override def drawGuiContainerBackgroundLayer(partialTicks: Float, mouseX: Int, mouseY: Int): Unit = {
    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F)
    this.mc.getTextureManager.bindTexture(LOCATION)
    this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize)
  }

  override def drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float): Unit = {
    this.drawDefaultBackground()
    super.drawScreen(mouseX, mouseY, partialTicks)
    this.renderHoveredToolTip(mouseX, mouseY)
  }

  override def drawGuiContainerForegroundLayer(p_146979_1_ : Int, p_146979_2_ : Int): Unit = {
    val s = getContainer.tile.getBlockType.getLocalizedName
    this.font.drawString(s, this.xSize / 2 - this.font.getStringWidth(s) / 2, 6, 0x404040)
    this.font.drawString(I18n.format(TranslationKeys.CONTAINER_INVENTORY), 8, this.ySize - 96 + 2, 0x404040)
    listContent().zipWithIndex.map { case (str, i) => str -> (i * 9 + 15) }
      .foreach { case (str, i) => this.font.drawString(str, 8, i, 0x404040) }
  }

  def listContent(): Seq[String] = {
    Option(getContainer.tile).collect { case p: StatusContainer.StatusProvider => p.getStatusStrings.map(TextFormatting.getTextWithoutFormattingCodes) }.getOrElse(Nil)
  }

  private def font = this.fontRenderer

  private def getContainer = this.inventorySlots.asInstanceOf[StatusContainer]
}
