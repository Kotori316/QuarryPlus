/*
 * Copyright (C) 2012,2013 yogpstop This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.yogpc.qp.gui

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiSlot
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

@SideOnly(Side.CLIENT)
class GuiSlotEntityList(mc: Minecraft, width: Int, height: Int, topIn: Int, bottomIn: Int, gc: GuiController) extends GuiSlot(mc, width, height, topIn, bottomIn, 18) {
    var selected = 0

    override protected def getSize: Int = this.gc.names.size

    override protected def elementClicked(slotIndex: Int, isDoubleClick: Boolean, mouseX: Int, mouseY: Int): Unit = this.selected = slotIndex

    override protected def isSelected(slotIndex: Int): Boolean = this.selected == slotIndex

    override protected def drawBackground(): Unit = ()

    override protected def getContentHeight: Int = this.getSize * 18

    override protected def drawSlot(entryID: Int, insideLeft: Int, yPos: Int, insideSlotHeight: Int, mouseXIn: Int, mouseYIn: Int, f: Float): Unit = {
        val name = this.gc.names.get(entryID)
        Minecraft.getMinecraft.fontRenderer.drawStringWithShadow(name,
            (this.mc.currentScreen.width - Minecraft.getMinecraft.fontRenderer.getStringWidth(name)) / 2,
            yPos + 2, 0xFFFFFF)
    }
}
