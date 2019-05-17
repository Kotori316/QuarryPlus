package com.yogpc.qp.machines.bookmover

import com.yogpc.qp.machines.TranslationKeys
import com.yogpc.qp.machines.base._
import com.yogpc.qp.utils.Holder
import com.yogpc.qp.{Config, _}
import net.minecraft.enchantment.{EnchantmentHelper, EnumEnchantmentType}
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.init.Items
import net.minecraft.inventory.ItemStackHelper
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.text.TextComponentTranslation
import net.minecraft.util.{ITickable, NonNullList}
import net.minecraft.world.IInteractionObject
import net.minecraftforge.registries.ForgeRegistries

import scala.collection.JavaConverters._

class TileBookMover extends APowerTile(Holder.bookMoverType) with HasInv with ITickable with IInteractionObject {

  val inv = NonNullList.withSize(getSizeInventory, ItemStack.EMPTY)
  configure(Config.common.workbenchMaxReceive.get() * APowerTile.MicroJtoMJ, 50000 * APowerTile.MicroJtoMJ)
  val enchTypes = EnumEnchantmentType.values().filter(_.canEnchantItem(Items.DIAMOND_PICKAXE)).toSet
  val validEnch = ForgeRegistries.ENCHANTMENTS.getValues.asScala.filter(e => enchTypes(e.`type`)).toSet

  override def write(nbt: NBTTagCompound): NBTTagCompound = {
    ItemStackHelper.saveAllItems(nbt, inv)
    super.write(nbt)
  }

  override def read(nbt: NBTTagCompound): Unit = {
    super.read(nbt)
    ItemStackHelper.loadAllItems(nbt, inv)
  }

  override def tick(): Unit = {
    super.tick()
    if (!world.isRemote && isWorking) {
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
          useEnergy(getMaxStored, getMaxStored, true, EnergyUsage.BOOK_MOVER)
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

  override def getName = new TextComponentTranslation(TranslationKeys.moverfrombook)

  override def getDisplayName = getName

  override def canReceive: Boolean = isWorking

  override def createContainer(playerInventory: InventoryPlayer, playerIn: EntityPlayer) = new ContainerBookMover(this, playerIn)

  override def getGuiID = BlockBookMover.GUI_ID
}

/*
 Test command.
 1 Fortune /give @p minecraft:enchanted_book{StoredEnchantments:[{id:"minecraft:fortune",lvl:6}]}
 2 Unbreaking /give @p minecraft:enchanted_book{StoredEnchantments:[{id:34,lvl:6}]}
 3 Fortune and Unbreaking /give @p minecraft:enchanted_book{StoredEnchantments:[{id:"minecraft:unbreaking",lvl:12}, {id:"minecraft:fortune",lvl:8}]}
 */
