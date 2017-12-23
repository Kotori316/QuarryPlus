package com.yogpc.qp.gui

import com.yogpc.qp.QuarryPlus
import com.yogpc.qp.container.ContainerAdvQuarry
import com.yogpc.qp.packet.PacketHandler
import com.yogpc.qp.packet.advquarry.AdvRangeMessage
import com.yogpc.qp.tile.TileAdvQuarry
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.resources.I18n
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.ChunkPos
import net.minecraft.util.{EnumFacing, ResourceLocation}
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

@SideOnly(Side.CLIENT)
class GuiAdvQuarry(tile: TileAdvQuarry, player: EntityPlayer) extends GuiContainer(new ContainerAdvQuarry(tile, player)) {

    val LOCATION = new ResourceLocation(QuarryPlus.modID, "textures/gui/chunkdestroyer.png")

    //7,15 to 168,70 box : 162, 56

    override def drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float): Unit = {
        //        this.drawDefaultBackground()
        super.drawScreen(mouseX, mouseY, partialTicks)
        //        this.renderHoveredToolTip(mouseX, mouseY)
    }

    override def drawGuiContainerBackgroundLayer(p_146976_1_ : Float, p_146976_2_ : Int, p_146976_3_ : Int): Unit = {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F)
        this.mc.getTextureManager.bindTexture(LOCATION)
        this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize)
    }

    override def initGui(): Unit = {
        super.initGui()
        buttonList.add(new GuiButton(0, guiLeft + 98, guiTop + 16, 10, 8, "+"))
        buttonList.add(new GuiButton(1, guiLeft + 68, guiTop + 16, 10, 8, "-"))
        buttonList.add(new GuiButton(2, guiLeft + 98, guiTop + 62, 10, 8, "+"))
        buttonList.add(new GuiButton(3, guiLeft + 68, guiTop + 62, 10, 8, "-"))
        buttonList.add(new GuiButton(4, guiLeft + 38, guiTop + 39, 10, 8, "+"))
        buttonList.add(new GuiButton(5, guiLeft + 8, guiTop + 39, 10, 8, "-"))
        buttonList.add(new GuiButton(6, guiLeft + 158, guiTop + 39, 10, 8, "+"))
        buttonList.add(new GuiButton(7, guiLeft + 128, guiTop + 39, 10, 8, "-"))
    }

    private def range = tile.digRange

    override def actionPerformed(button: GuiButton): Unit = {
        super.actionPerformed(button)
        if (tile.mode is TileAdvQuarry.NOTNEEDBREAK) {
            val direction = EnumFacing.getFront(button.id / 2 + 2)
            val increase = if (button.id % 2 == 0) 1 else -1
            val t = 16 * increase

            if (range.defined) {
                val newRange =
                    if (direction.getAxis == EnumFacing.Axis.X) {
                        if (direction.getAxisDirection == EnumFacing.AxisDirection.POSITIVE) {
                            val e = range.maxX
                            if (range.minX < e + t) range.copy(maxX = e + t) else range
                        } else {
                            val e = range.minX
                            if (range.maxX > e - t) range.copy(minX = e - t) else range
                        }
                    } else if (direction.getAxis == EnumFacing.Axis.Z) {
                        if (direction.getAxisDirection == EnumFacing.AxisDirection.POSITIVE) {
                            val e = range.maxZ
                            if (range.minZ < e + t) range.copy(maxZ = e + t) else range
                        } else {
                            val e = range.minZ
                            if (range.maxZ > e - t) range.copy(minZ = e - t) else range
                        }
                    } else range
                tile.digRange = newRange
                PacketHandler.sendToServer(AdvRangeMessage.create(tile))
            }
        }
    }

    override def drawGuiContainerForegroundLayer(mouseX: Int, mouseY: Int) {
        val s: String = I18n.format("tile.chunkdestroyer.name")
        this.fontRendererObj.drawString(s, this.xSize / 2 - this.fontRendererObj.getStringWidth(s) / 2, 6, 0x404040)
        this.fontRendererObj.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, 0x404040)
        if (range.defined) {
            val chunkPos = new ChunkPos(tile.getPos)
            val north: Double = chunkPos.getZStart - range.minZ
            val south: Double = range.maxZ - chunkPos.getZEnd
            val east: Double = range.maxX - chunkPos.getXEnd
            val west: Double = chunkPos.getXStart - range.minX
            this.fontRendererObj.drawString((north / 16).toString, 79, 17, 0x404040)
            this.fontRendererObj.drawString((south / 16).toString, 79, 63, 0x404040)
            this.fontRendererObj.drawString((west / 16).toString, 19, 40, 0x404040)
            this.fontRendererObj.drawString((east / 16).toString, 139, 40, 0x404040)
        }
    }
}
