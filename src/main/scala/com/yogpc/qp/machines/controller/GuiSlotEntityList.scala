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
package com.yogpc.qp.machines.controller

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.SlotGui
import net.minecraftforge.api.distmarker.{Dist, OnlyIn}

@OnlyIn(Dist.CLIENT)
class GuiSlotEntityList(mc: Minecraft, width: Int, height: Int, topIn: Int, bottomIn: Int, gc: GuiController)
  extends SlotGui(mc, width, height, topIn, bottomIn, 18) {
  var selected = 0

  /*override protected def getSize: Int = this.gc.names.size

  override def setSelectedEntry(slotIndex: Int): Unit = this.selected = slotIndex

  override protected def isSelected(slotIndex: Int): Boolean = this.selected == slotIndex

  override protected def drawBackground(): Unit = ()

  override protected def getContentHeight: Int = this.getSize * 18

  override protected def drawSlot(entryID: Int, insideLeft: Int, yPos: Int, insideSlotHeight: Int, mouseXIn: Int, mouseYIn: Int, f: Float): Unit = {

  }*/
  override def getItemCount: Int = this.gc.names.size

  override def selectItem(slotIndex: Int, p_selectItem_2_ : Int, p_selectItem_3_ : Double, p_selectItem_5_ : Double) = {
    this.selected = slotIndex
    super.selectItem(slotIndex, p_selectItem_2_, p_selectItem_3_, p_selectItem_5_)
  }

  override def isSelectedItem(slotIndex: Int) = this.selected == slotIndex

  override def renderBackground(): Unit = ()

  override def getMaxPosition = this.getItemCount * this.getItemHeight

  override def renderItem(entryID: Int, insideLeft: Int, yPos: Int, insideSlotHeight: Int, mouseXIn: Int, mouseYIn: Int, f: Float): Unit = {
    val name = this.gc.names.get(entryID).toString
    Minecraft.getInstance().fontRenderer.drawStringWithShadow(name,
      (this.mc.currentScreen.width - Minecraft.getInstance.fontRenderer.getStringWidth(name)) / 2,
      yPos + 2, 0xFFFFFF)
  }
}
