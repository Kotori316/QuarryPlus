package com.yogpc.qp.gui

import com.yogpc.qp.QuarryPlus
import com.yogpc.qp.packet.PacketHandler
import com.yogpc.qp.packet.advquarry.AdvFilterMessage
import com.yogpc.qp.tile.TileAdvQuarry
import net.minecraft.client.gui.{GuiButton, GuiSlot}
import net.minecraft.client.resources.I18n
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.EnumFacing
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

@SideOnly(Side.CLIENT)
class GuiAdvQuarryFluid(tile: TileAdvQuarry, player: EntityPlayer, val facing: EnumFacing) extends GuiScreenA(null) {

    val filter = tile.fluidExtractFacings(facing)
    var fluidList: FluidSlot = _

    val DONE_ID = 0
    val ADDFROMLIST_ID = 2
    val REMOVE_ID = 3

    override def initGui(): Unit = {
        super.initGui()
        fluidList = new FluidSlot
        buttonList.add(new GuiButton(DONE_ID, this.width / 2 - 50, this.height - 26, 100, 20,
            I18n.format(TranslationKeys.DONE)))
        buttonList.add(new GuiButton(ADDFROMLIST_ID, this.width * 2 / 3 + 10, 20, 100, 20,
            I18n.format(TranslationKeys.ADD) + "(" + I18n.format(TranslationKeys.FROM_LIST) + ")"))
        buttonList.add(new GuiButton(REMOVE_ID, this.width * 2 / 3 + 10, 70, 100, 20,
            I18n.format(TranslationKeys.DELETE)))
    }

    override def actionPerformed(button: GuiButton): Unit = {
        super.actionPerformed(button)
        button.id match {
            case DONE_ID =>
                showParent()
            case ADDFROMLIST_ID =>
                mc.displayGuiScreen(new SelectFluid(this, tile))
            case REMOVE_ID =>
                fluidList.current.foreach(i => filter.remove(filter.toSeq(i)))
                PacketHandler.sendToServer(AdvFilterMessage.create(tile))
            case _ => QuarryPlus.LOGGER.error(s"Strange button id ${button.id}")
        }
    }

    /**
      * Handles mouse input.
      */
    override def handleMouseInput(): Unit = {
        super.handleMouseInput()
        this.fluidList.handleMouseInput()
    }

    override def drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float): Unit = {
        drawDefaultBackground()
        fluidList.drawScreen(mouseX, mouseY, partialTicks)
        super.drawScreen(mouseX, mouseY, partialTicks)
        drawCenteredString(this.fontRenderer, I18n.format("FD." + facing), this.width / 2, 8, 0xFFFFFF)
    }

    class FluidSlot extends GuiSlot(this.mc, this.width * 3 / 5, this.height, 30, this.height - 30, 18) {
        var current: Option[Int] = None

        override def drawBackground(): Unit = drawDefaultBackground()

        override def elementClicked(slotIndex: Int, isDoubleClick: Boolean, mouseX: Int, mouseY: Int): Unit = current = Some(slotIndex)

        override def drawSlot(slotIndex: Int, xPos: Int, yPos: Int, heightIn: Int, mouseXIn: Int, mouseYIn: Int, partialTicks: Float): Unit = {
            filter.map(_.getLocalizedName).foreach(name =>
                mc.fontRenderer.drawStringWithShadow(name, (GuiAdvQuarryFluid.this.width * 3 / 5 - mc.fontRenderer.getStringWidth(name)) / 2, yPos + 2, 0xFFFFFF)
            )
        }

        override def isSelected(slotIndex: Int): Boolean = current contains slotIndex

        override def getSize: Int = filter.size
    }

}

private class SelectFluid(parentAdv: GuiAdvQuarryFluid, tile: TileAdvQuarry) extends GuiScreenA(parentAdv) {
    val DONE_ID = 0
    val CANCEL_ID: Int = 1
    var fluidList: FluidSlot = _

    override def initGui(): Unit = {
        super.initGui()
        fluidList = new FluidSlot
        this.buttonList.add(new GuiButton(DONE_ID, this.width / 2 - 150, this.height - 26, 140, 20, I18n.format(TranslationKeys.DONE)))
        this.buttonList.add(new GuiButton(CANCEL_ID, this.width / 2 + 10, this.height - 26, 140, 20, I18n.format(TranslationKeys.CANCEL)))
    }

    override def drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float): Unit = {
        drawDefaultBackground()
        fluidList.drawScreen(mouseX, mouseY, partialTicks)
        super.drawScreen(mouseX, mouseY, partialTicks)
        drawCenteredString(this.fontRenderer, I18n.format("FD." + parentAdv.facing), this.width / 2, 8, 0xFFFFFF)
    }

    /**
      * Handles mouse input.
      */
    override def handleMouseInput(): Unit = {
        super.handleMouseInput()
        this.fluidList.handleMouseInput()
    }

    override def actionPerformed(button: GuiButton): Unit = {
        if (button.id == DONE_ID) {
            Option(fluidList.currentFluid).map { s => val c = s.copy(); c.amount = 1000; c }.foreach(parentAdv.filter.add)
            PacketHandler.sendToServer(AdvFilterMessage.create(tile))
        }
        showParent()
    }

    class FluidSlot extends GuiSlot(this.mc, this.width, this.height, 24, this.height - 32, 18) {
        private val addable = {
            tile.fluidStacks.keySet.diff(parentAdv.filter).toSeq
        }
        var currentFluid: FluidStack = _

        override def drawBackground(): Unit = parentAdv.drawDefaultBackground()

        override def elementClicked(slotIndex: Int, isDoubleClick: Boolean, mouseX: Int, mouseY: Int): Unit = currentFluid = addable(slotIndex)

        override def drawSlot(slotIndex: Int, xPos: Int, yPos: Int, heightIn: Int, mouseXIn: Int, mouseYIn: Int, partialTicks: Float): Unit = {
            val name = addable(slotIndex).getLocalizedName
            mc.fontRenderer.drawStringWithShadow(name, (SelectFluid.this.width - mc.fontRenderer.getStringWidth(name)) / 2, yPos + 2, 0xFFFFFF)
        }

        override def isSelected(slotIndex: Int): Boolean = addable(slotIndex) == currentFluid

        override def getSize: Int = addable.size

    }

}
