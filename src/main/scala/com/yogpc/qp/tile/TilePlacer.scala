package com.yogpc.qp.tile

import javax.annotation.{Nonnull, Nullable}

import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.{IInventory, ItemStackHelper}
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.{EnumFacing, NonNullList}
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.items.CapabilityItemHandler
import net.minecraftforge.items.wrapper.InvWrapper

import scala.collection.JavaConverters._

class TilePlacer extends TileEntity with IInventory {
    private val inventory = NonNullList.withSize(getSizeInventory, ItemStack.EMPTY)
    private val handler = new InvWrapper(this)

    override def readFromNBT(compound: NBTTagCompound): Unit = {
        super.readFromNBT(compound)
        ItemStackHelper.loadAllItems(compound, inventory)
    }

    override def writeToNBT(compound: NBTTagCompound): NBTTagCompound = {
        ItemStackHelper.saveAllItems(compound, inventory)
        super.writeToNBT(compound)
    }

    override def getSizeInventory = 9

    override def isEmpty: Boolean = inventory.asScala.forall(_.isEmpty)

    @Nonnull override def getStackInSlot(index: Int): ItemStack = inventory.get(index)

    @Nonnull override def decrStackSize(index: Int, count: Int): ItemStack = ItemStackHelper.getAndSplit(inventory, index, count)

    @Nonnull override def removeStackFromSlot(index: Int): ItemStack = ItemStackHelper.getAndRemove(inventory, index)

    override def setInventorySlotContents(index: Int, @Nonnull stack: ItemStack): Unit = inventory.set(index, stack)

    override def getInventoryStackLimit = 64

    override def isUsableByPlayer(player: EntityPlayer): Boolean = (getWorld.getTileEntity(getPos) eq this) && player.getDistanceSqToCenter(getPos) <= 64

    override def openInventory(player: EntityPlayer) = ()

    override def closeInventory(player: EntityPlayer) = ()

    override def isItemValidForSlot(index: Int, stack: ItemStack) = true

    override def getField(id: Int) = 0

    override def setField(id: Int, value: Int) = ()

    override def getFieldCount = 0

    override def clear() = inventory.clear()

    override def getName = "tile.placerplus.name"

    override def hasCustomName = false

    override def hasCapability(capability: Capability[_], @Nullable facing: EnumFacing): Boolean =
        (capability eq CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) || super.hasCapability(capability, facing)

    @Nullable override def getCapability[T](capability: Capability[T], @Nullable facing: EnumFacing): T =
        if (capability eq CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(handler)
        else super.getCapability(capability, facing)
}