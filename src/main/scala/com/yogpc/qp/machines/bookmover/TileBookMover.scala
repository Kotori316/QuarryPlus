package com.yogpc.qp.machines.bookmover

import com.yogpc.qp.machines.base._
import com.yogpc.qp.machines.{PowerManager, TranslationKeys}
import com.yogpc.qp.utils.Holder
import com.yogpc.qp.{Config, _}
import net.minecraft.enchantment.{EnchantmentHelper, EnchantmentType}
import net.minecraft.entity.player.{PlayerEntity, PlayerInventory}
import net.minecraft.inventory.ItemStackHelper
import net.minecraft.inventory.container.{Container, INamedContainerProvider}
import net.minecraft.item.{ItemStack, Items}
import net.minecraft.nbt.CompoundNBT
import net.minecraft.tileentity.ITickableTileEntity
import net.minecraft.util.NonNullList
import net.minecraft.util.text.TranslationTextComponent
import net.minecraftforge.registries.ForgeRegistries

import scala.jdk.CollectionConverters._

class TileBookMover extends APowerTile(Holder.bookMoverType) with HasInv with ITickableTileEntity with INamedContainerProvider {

  val inv = NonNullList.withSize(getSizeInventory, ItemStack.EMPTY)
  configure(Config.common.workbenchMaxReceive.get() * APowerTile.MJToMicroMJ, 50000 * APowerTile.MJToMicroMJ)
  val enchTypes = EnchantmentType.values().filter(_.canEnchantItem(Items.DIAMOND_PICKAXE)).toSet
  val validEnch = ForgeRegistries.ENCHANTMENTS.getValues.asScala.filter(e => enchTypes(e.`type`)).toSet

  override def write(nbt: CompoundNBT): CompoundNBT = {
    ItemStackHelper.saveAllItems(nbt, inv)
    super.write(nbt)
  }

  override def read(nbt: CompoundNBT): Unit = {
    super.read(nbt)
    ItemStackHelper.loadAllItems(nbt, inv)
  }

  override def workInTick(): Unit = {
    if (isWorking) {
      startWork()
      if (enabled && getStoredEnergy >= getMaxStored) {
        if (!isItemValidForSlot(0, inv.get(0)) || !isItemValidForSlot(1, inv.get(1)))
          return
        val enchList = EnchantmentHelper.getEnchantments(inv.get(1)).asScala
        enchList.find { case (e, level) =>
          validEnch.contains(e) &&
            EnchantmentHelper.getEnchantments(inv.get(0)).asScala.forall { case (e1, _) => e1 == e || e1.isCompatibleWith(e) } &&
            EnchantmentHelper.getEnchantmentLevel(e, inv.get(0)) < level
        }.foreach { case (e, level) =>
          val copy = inv.get(0).copy()
          copy.removeEnchantment(e)
          copy.addEnchantment(e, level)
          if (enchList.size == 1) {
            setInventorySlotContents(1, new ItemStack(Items.BOOK))
          } else {
            inv.get(1).removeEnchantment(e)
          }
          setInventorySlotContents(0, ItemStack.EMPTY)
          setInventorySlotContents(2, copy)
          PowerManager.useEnergy(this, getMaxStored, EnergyUsage.BOOK_MOVER)
          finishWork()
        }
      }
    }
  }

  override def getSizeInventory: Int = 3

  override def isEmpty: Boolean = inv.asScala.forall(_.isEmpty)

  override def getStackInSlot(index: Int): ItemStack = inv.get(index)

  override def decrStackSize(index: Int, count: Int): ItemStack = ItemStackHelper.getAndSplit(inv, index, count)

  override def removeStackFromSlot(index: Int): ItemStack = ItemStackHelper.getAndRemove(inv, index)

  override def setInventorySlotContents(index: Int, stack: ItemStack): Unit = inv.set(index, stack)

  override def getInventoryStackLimit: Int = 1

  override def clear(): Unit = inv.clear()

  override def isItemValidForSlot(index: Int, stack: ItemStack): Boolean = {
    val item = stack.getItem
    index match {
      case 0 => item.isInstanceOf[IEnchantableItem] && item.asInstanceOf[IEnchantableItem].isValidInBookMover
      case 1 => item == Items.ENCHANTED_BOOK
      case _ => false
    }
  }

  override def isWorking: Boolean = {
    enabled() && !inv.get(0).isEmpty && !inv.get(1).isEmpty
  }

  override def getName = new TranslationTextComponent(TranslationKeys.moverfrombook)

  override def getDisplayName = getName

  override def canReceive: Boolean = isWorking

  override def createMenu(id: Int, i: PlayerInventory, player: PlayerEntity): Container = {
    new ContainerBookMover(id, player, pos)
  }
}

/*
 Test command.
 1 Fortune /give @p minecraft:enchanted_book{StoredEnchantments:[{id:"minecraft:fortune",lvl:6}]}
 2 Unbreaking /give @p minecraft:enchanted_book{StoredEnchantments:[{id:34,lvl:6}]}
 3 Fortune and Unbreaking /give @p minecraft:enchanted_book{StoredEnchantments:[{id:"minecraft:unbreaking",lvl:12}, {id:"minecraft:fortune",lvl:8}]}
 */
