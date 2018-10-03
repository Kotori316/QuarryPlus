package com.yogpc.qp.tile

import com.yogpc.qp.block.BlockBookMover
import com.yogpc.qp.gui.TranslationKeys
import com.yogpc.qp.item.IEnchantableItem
import com.yogpc.qp.version.VersionUtil
import com.yogpc.qp.{Config, _}
import net.minecraft.enchantment.{EnchantmentHelper, EnumEnchantmentType}
import net.minecraft.init.Items
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.ITickable
import net.minecraftforge.fml.common.registry.ForgeRegistries

import scala.collection.JavaConverters._

class TileBookMover extends APowerTile with HasInv with ITickable {

    val inv = NonNullList.withSize(getSizeInventory, VersionUtil.empty())
    configure(Config.content.workbenchMaxReceive, 50000)
    val enchTypes = EnumEnchantmentType.values().filter(_.canEnchantItem(Items.DIAMOND_PICKAXE)).toSet
    val validEnch = ForgeRegistries.ENCHANTMENTS.getValues.asScala.filter(e => enchTypes(e.`type`)).toSet

    override def writeToNBT(nbttc: NBTTagCompound): NBTTagCompound = {
        ItemStackHelper.saveAllItems(nbttc, inv)
        super.writeToNBT(nbttc)
    }

    override def readFromNBT(nbttc: NBTTagCompound): Unit = {
        super.readFromNBT(nbttc)
        ItemStackHelper.loadAllItems(nbttc, inv)
    }

    override def update(): Unit = {
        super.update()
        if (!worldObj.isRemote && isWorking) {
            startWork()
            if (!machineDisabled && getStoredEnergy >= getMaxStored) {
                if (!isItemValidForSlot(0, inv.get(0)) || !isItemValidForSlot(1, inv.get(1)))
                    return
                val enchList = EnchantmentHelper.getEnchantments(inv.get(1)).asScala
                enchList.find { case (e, level) =>
                    validEnch.contains(e) &&
                      EnchantmentHelper.getEnchantments(inv.get(0)).asScala.forall { case (e1, _) => e1 == e || e1.canApplyTogether(e) } &&
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
                    setInventorySlotContents(0, VersionUtil.empty())
                    setInventorySlotContents(2, copy)
                    useEnergy(getMaxStored, getMaxStored, true, EnergyUsage.BOOKMOVER)
                    finishWork()
                }
            }
        }
    }

    override def getSizeInventory: Int = 3

    override def isEmpty: Boolean = inv.asScala.forall(VersionUtil.isEmpty)

    override def getStackInSlot(index: Int): ItemStack = inv.get(index)

    override def decrStackSize(index: Int, count: Int): ItemStack = ItemStackHelper.getAndSplit(inv, index, count)

    override def removeStackFromSlot(index: Int): ItemStack = ItemStackHelper.getAndRemove(inv, index)

    override def setInventorySlotContents(index: Int, stack: ItemStack): Unit = inv.set(index, stack)

    override def getInventoryStackLimit: Int = 1

    override def clear(): Unit = inv.clear()

    override def isItemValidForSlot(index: Int, stack: ItemStack): Boolean = {
        index match {
            case 0 => stack != null && stack.getItem.isInstanceOf[IEnchantableItem]
            case 1 => stack != null && stack.getItem == Items.ENCHANTED_BOOK
            case _ => false
        }
    }

    override protected def isWorking: Boolean = {
        VersionUtil.nonEmpty(inv.get(0)) && VersionUtil.nonEmpty(inv.get(1))
    }

    override protected def getSymbol: Symbol = BlockBookMover.SYMBOL

    override def getName: String = TranslationKeys.moverfrombook

    override def canReceive: Boolean = isWorking
}

/*
 Test command.
 1 Fortune /give @p minecraft:enchanted_book 1 0 {StoredEnchantments:[{id:35,lvl:6}]}
 2 Unbreaking /give @p minecraft:enchanted_book 1 0 {StoredEnchantments:[{id:34,lvl:6}]}
 3 Fortune and Unbreaking /give @p minecraft:enchanted_book 1 0 {StoredEnchantments:[{id:34,lvl:12}, {id:35,lvl:8}]}
 */
