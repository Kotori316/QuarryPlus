package com.yogpc.qp.gui

import com.yogpc.qp.QuarryPlus
import com.yogpc.qp.container.ContainerAdvPump
import com.yogpc.qp.tile.TileAdvPump
import net.minecraft.client.gui.GuiButton
import net.minecraft.client.gui.inventory.GuiContainer
import net.minecraft.client.resources.I18n
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11

class GuiAdvPump(tile: TileAdvPump, player: EntityPlayer) extends GuiContainer(new ContainerAdvPump(tile, player)) {

    val LOCATION = new ResourceLocation(QuarryPlus.modID, "textures/gui/advpump.png")

    override def drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float): Unit = {
        this.drawDefaultBackground()
        super.drawScreen(mouseX, mouseY, partialTicks)
        this.renderHoveredToolTip(mouseX, mouseY)
    }

    override def initGui(): Unit = {
        this.buttonList.add(new GuiButton(1, 12, 12, 100, 20, "PlaceFrame = " + tile.placeFrame))
    }

    override def actionPerformed(button: GuiButton): Unit = {
        button.id match {
            case 1 => tile.placeFrame = !tile.placeFrame
                this.buttonList.get(0).displayString = "PlaceFrame = " + tile.placeFrame
        }
    }

    override def drawGuiContainerForegroundLayer(mouseX: Int, mouseY: Int): Unit = {
        val s: String = I18n.format("tile.standalonepump.name")
        this.fontRenderer.drawString(s, this.xSize / 2 - this.fontRenderer.getStringWidth(s) / 2, 6, 4210752)
    }

    override def drawGuiContainerBackgroundLayer(partialTicks: Float, mouseX: Int, mouseY: Int) = {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F)
        this.mc.getTextureManager.bindTexture(LOCATION)
        this.drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize)
    }
}
