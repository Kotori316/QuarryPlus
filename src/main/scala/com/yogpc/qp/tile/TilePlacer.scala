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

import com.yogpc.qp.Config
import com.yogpc.qp.block.ADismCBlock
import com.yogpc.qp.gui.TranslationKeys
import com.yogpc.qp.version.VersionUtil
import javax.annotation.{Nonnull, Nullable}
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.init.Blocks
import net.minecraft.inventory.ItemStackHelper
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.{EnumActionResult, EnumFacing, EnumHand, NonNullList}
import net.minecraft.world.{World, WorldServer}
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.items.CapabilityItemHandler
import net.minecraftforge.items.wrapper.InvWrapper

import scala.collection.JavaConverters._

class TilePlacer extends TileEntity with HasInv {
  tile =>
  private[this] val inventory = NonNullList.withSize(getSizeInventory, com.yogpc.qp.version.VersionUtil.empty())
  private[this] val handler = new InvWrapper(this)
  private[this] val isAir = (world: World, pos: BlockPos) => {
    val state = world.getBlockState(pos)
    state.getBlock.isAir(state, world, pos)
  }
  private[this] lazy val fakePlayer = QuarryFakePlayer.get(getWorld.asInstanceOf[WorldServer])

  def updateTick(): Unit = {
    val facing = getWorld.getBlockState(getPos).getValue(ADismCBlock.FACING)
    val facing1 = EnumFacing.getFront(facing.getIndex + 2)
    val facing2 = EnumFacing.getFront(facing.getIndex + 4)
    val playerInvCopy = new PlayerInvCopy(fakePlayer.inventory)
    playerInvCopy.setItems(tile)
    var lastIndex = 0
    inventory.asScala.zipWithIndex.exists { case (is, i) =>
      def t(): Boolean = {
        lastIndex = i
        fakePlayer.inventory.currentItem = i
        val offset = getPos.offset(facing)
        val facingList = List(facing.getOpposite, facing1, facing1.getOpposite, facing2, facing2.getOpposite)
        if (VersionUtil.nonEmpty(is)) {
          val onitemusefirst = (enumfacing: EnumFacing) =>
            is.getItem.onItemUseFirst(fakePlayer, getWorld, getPos.offset(enumfacing), enumfacing.getOpposite,
              0.5F, 0.5F, 0.5F, EnumHand.MAIN_HAND) == EnumActionResult.SUCCESS
          if (onitemusefirst(facing))
            return true
          if (!Config.content.placerOnlyPlaceFront) {
            if (facingList exists onitemusefirst)
              return true
          }
        }
        val k = getWorld.getBlockState(offset)
        if (k.getBlock.onBlockActivated(getWorld, offset, k, fakePlayer, EnumHand.MAIN_HAND, facing.getOpposite, 0.5F, 0.5F, 0.5F))
          return true
        var flagPlacedDummyBlock = false
        if (Config.content.placerOnlyPlaceFront && isAir(getWorld, offset.down)) {
          getWorld.setBlockState(offset.down, Blocks.BARRIER.getDefaultState)
          flagPlacedDummyBlock = true
        }

        def itemUse(worldIn: World, pos: BlockPos, facing: EnumFacing, facing1: EnumFacing, facing2: EnumFacing, player: EntityPlayer, is: ItemStack): Boolean = {
          val onitemuse = (enumFacing: EnumFacing) => is.onItemUse(player, getWorld, getPos.offset(enumFacing),
            EnumHand.MAIN_HAND, enumFacing.getOpposite, 0.5f, 0.5f, 0.5f) == EnumActionResult.SUCCESS
          if (onitemuse(facing)) true
          //Do you want to place block on non-facing side?
          else if (!Config.content.placerOnlyPlaceFront)
            if (facingList exists onitemuse) true
            else false
          else
            false
        }

        if (itemUse(getWorld, getPos, facing, facing1, facing2, fakePlayer, is)) {
          if (flagPlacedDummyBlock) getWorld.setBlockToAir(offset.down)
          return true
        } else if (flagPlacedDummyBlock) {
          getWorld.setBlockToAir(offset.down)
        }

        val value = is.useItemRightClick(getWorld, fakePlayer, EnumHand.MAIN_HAND)
        inventory.set(i, value.getResult)
        value.getType == EnumActionResult.SUCCESS
      }

      t()
    }
    if (lastIndex < tile.getSizeInventory)
      if (VersionUtil.isEmpty(fakePlayer.inventory.getCurrentItem))
        tile.setInventorySlotContents(lastIndex, VersionUtil.empty)
    playerInvCopy.resetItems()
  }

  override def readFromNBT(compound: NBTTagCompound): Unit = {
    super.readFromNBT(compound)
    ItemStackHelper.loadAllItems(compound, inventory)
  }

  override def writeToNBT(compound: NBTTagCompound): NBTTagCompound = {
    ItemStackHelper.saveAllItems(compound, inventory)
    super.writeToNBT(compound)
  }

  override def getSizeInventory = 9

  override def isEmpty: Boolean = inventory.asScala.forall(VersionUtil.isEmpty)

  @Nonnull override def getStackInSlot(index: Int): ItemStack = inventory.get(index)

  @Nonnull override def decrStackSize(index: Int, count: Int): ItemStack = ItemStackHelper.getAndSplit(inventory, index, count)

  @Nonnull override def removeStackFromSlot(index: Int): ItemStack = ItemStackHelper.getAndRemove(inventory, index)

  override def setInventorySlotContents(index: Int, @Nonnull stack: ItemStack): Unit = inventory.set(index, stack)

  override def getInventoryStackLimit = 64

  override def isItemValidForSlot(index: Int, stack: ItemStack) = true

  override def clear() = inventory.clear()

  override def getName = TranslationKeys.placer

  override def isUsableByPlayer(player: EntityPlayer) = getWorld.getTileEntity(getPos) eq this

  override def hasCapability(capability: Capability[_], @Nullable facing: EnumFacing): Boolean =
    (capability eq CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) || super.hasCapability(capability, facing)

  @Nullable override def getCapability[T](capability: Capability[T], @Nullable facing: EnumFacing): T =
    if (capability eq CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(handler)
    else super.getCapability(capability, facing)
}

class PlayerInvCopy(inventory: InventoryPlayer) {

  def setItems(placer: TilePlacer): Unit = {
    resetItems()
    for (i <- 0 until 9) {
      inventory.mainInventory.set(i, placer.getStackInSlot(i))
    }
  }

  def resetItems(): Unit = {
    inventory.currentItem = 0
    inventory.clear()
  }
}
