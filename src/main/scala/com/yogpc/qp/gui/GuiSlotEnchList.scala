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

import com.yogpc.qp.utils.BlockData
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.{GuiScreen, GuiSlot}
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

@SideOnly(Side.CLIENT)
class GuiSlotEnchList(mc: Minecraft, w: Int, h: Int, t: Int, b: Int, val parent: GuiScreen, val target: java.util.List[BlockData]) extends GuiSlot(mc, w, h, t, b, 18) {
    var currentore = 0

    override protected def getSize: Int = this.target.size

    override protected def elementClicked(var1: Int, var2: Boolean, var3: Int, var4: Int): Unit = this.currentore = var1

    override protected def getContentHeight: Int = this.getSize * 18

    override protected def isSelected(var1: Int): Boolean = var1 == this.currentore

    override protected def drawBackground(): Unit = ()

    override protected def drawSlot(entryID: Int, insideLeft: Int, yPos: Int, insideSlotHeight: Int, mouseXIn: Int, mouseYIn: Int, f: Float): Unit = {
        val name = this.target.get(entryID).getLocalizedName
        Minecraft.getMinecraft.fontRenderer.drawStringWithShadow(name,
            (this.parent.width * 3 / 5 - Minecraft.getMinecraft.fontRenderer.getStringWidth(name)) / 2, yPos + 2, 0xFFFFFF)
    }
}
