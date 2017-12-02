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

package com.yogpc.qp.tile

import java.util
import javax.annotation.Nullable

import com.yogpc.qp.tile.IEnchantableTile._
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.EnumFacing
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.items.CapabilityItemHandler
import net.minecraftforge.items.wrapper.InvWrapper

class TileBreaker extends TileEntity with IEnchantableTile with HasInv {
    private[this] val handler = new InvWrapper(this)
    var silktouch = false
    var fortune: Byte = 0

    override def readFromNBT(nbttc: NBTTagCompound): Unit = {
        super.readFromNBT(nbttc)
        this.silktouch = nbttc.getBoolean("silktouch")
        this.fortune = nbttc.getByte("fortune")
    }

    override def writeToNBT(nbttc: NBTTagCompound): NBTTagCompound = {
        nbttc.setBoolean("silktouch", this.silktouch)
        nbttc.setByte("fortune", this.fortune)
        super.writeToNBT(nbttc)
    }

    override def getEnchantments: util.Map[java.lang.Integer, java.lang.Byte] = {
        val ret = new util.HashMap[java.lang.Integer, java.lang.Byte]
        if (this.fortune > 0) ret.put(FortuneID, this.fortune)
        if (this.silktouch) ret.put(SilktouchID, 1.toByte)
        ret
    }

    override def setEnchantent(id: Short, value: Short) =
        if (id == FortuneID) this.fortune = value.toByte
        else if (id == SilktouchID && value > 0) this.silktouch = true

    override def G_reinit() = ()

    override def getSizeInventory = 1

    override def isEmpty = true

    override def getStackInSlot(i: Int) = com.yogpc.qp.version.VersionUtil.empty()

    override def decrStackSize(i: Int, a: Int) = com.yogpc.qp.version.VersionUtil.empty()

    override def removeStackFromSlot(index: Int) = com.yogpc.qp.version.VersionUtil.empty()

    override def setInventorySlotContents(index: Int, stack: ItemStack) = ()

    override def getInventoryStackLimit = 0

    override def isItemValidForSlot(s: Int, stack: ItemStack) = false

    override def clear() = ()

    override def getName = "tile.breakerplus.name"

    override def isUsableByPlayer(player: EntityPlayer) = getWorld.getTileEntity(getPos) eq this

    override def hasCapability(capability: Capability[_], @Nullable facing: EnumFacing): Boolean =
        (capability eq CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) || super.hasCapability(capability, facing)

    @Nullable override def getCapability[T](capability: Capability[T], @Nullable facing: EnumFacing): T =
        if (capability eq CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(handler)
        else super.getCapability(capability, facing)
}