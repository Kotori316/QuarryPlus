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

package com.yogpc.qp.machines.base

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.{IInventory, Slot}
import net.minecraft.item._

class SlotUnlimited(inv: IInventory, num: Int, x: Int, y: Int) extends Slot(inv, num, x, y)

class SlotWorkbench(inv: IInventory, index: Int, xPosition: Int, yPosition: Int) extends Slot(inv, index, xPosition, yPosition) {
  override def isItemValid(is: ItemStack) = false

  override def canTakeStack(playerIn: EntityPlayer) = false
}

class SlotMover(inv: IInventory, index: Int, xPosition: Int, yPosition: Int) extends Slot(inv, index, xPosition, yPosition) {
  override def isItemValid(is: ItemStack): Boolean = {
    this.slotNumber match {
      case 0 =>
        if (is.getEnchantmentTagList != null) {
          is.getItem match {
            case tool: ItemTool => tool.getTier == ItemTier.DIAMOND
            case _: ItemBow => true
            case _ => false
          }
        } else {
          false
        }
      case 1 => is.getItem.isInstanceOf[IEnchantableItem]
      case _ => false
    }
  }

  override def getSlotStackLimit = 1
}

class SlotTile(inv: IInventory, index: Int, xPosition: Int, yPosition: Int) extends Slot(inv, index, xPosition, yPosition) {
  override def isItemValid(stack: ItemStack): Boolean = inv.isItemValidForSlot(index, stack)
}
