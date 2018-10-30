package com.yogpc.qp.tile

import java.util.Objects

import com.yogpc.qp.version.VersionUtil
import net.minecraft.block.Block
import net.minecraft.init.Blocks
import net.minecraft.item.{Item, ItemBlock, ItemStack}
import net.minecraft.nbt.NBTTagCompound
import net.minecraftforge.oredict.OreDictionary

sealed abstract class ItemDamage extends Ordered[ItemDamage] {
    val item: Item
    val damage: Int
    val tag: NBTTagCompound

    def anyMeta: Boolean = damage == OreDictionary.WILDCARD_VALUE

    def equals(any: Any): Boolean

    def hashCode(): Int

    def toStack(amount: Int = 1): ItemStack = {
        val s = new ItemStack(item, amount, damage)
        if (tag != null) {
            s.setTagCompound(tag.copy())
        }
        s
    }

    def itemStackLimit: Int = {
        item.getItemStackLimit(toStack())
    }

    override def compare(that: ItemDamage): Int = Integer.compare(Item.getIdFromItem(item), Item.getIdFromItem(that.item))
}

case class OK(itemStack: ItemStack) extends ItemDamage {

    val item: Item = itemStack.getItem
    val damage: Int = itemStack.getItemDamage
    val tag: NBTTagCompound = itemStack.getTagCompound

    override def toString: String = item.getUnlocalizedName() + "@" + damage

    override def equals(any: Any): Boolean = {
        any match {
            case itemdamage: OK =>
                if (hashCode() == itemdamage.hashCode())
                    if (Objects.equals(tag, itemdamage.tag))
                        return item == itemdamage.item && (anyMeta || itemdamage.damage == this.damage)
                false
            case _ => false
        }
    }

    override def hashCode(): Int = item.hashCode

    override def toStack(amount: Int): ItemStack = {
        val a = itemStack.copy()
        VersionUtil.setCount(a, amount)
        a
    }

    override def itemStackLimit: Int = item.getItemStackLimit(itemStack)
}

case class BlockOK(itemStack: ItemStack, block: Block) extends ItemDamage {
    val item: Item = Item.getItemFromBlock(block)
    val damage: Int = itemStack.getItemDamage
    val tag: NBTTagCompound = itemStack.getTagCompound

    override def toString: String = block.getUnlocalizedName + "@" + damage

    override def hashCode(): Int = block.hashCode()

    override def equals(any: Any): Boolean = {
        any match {
            case blockOK: BlockOK =>
                if (hashCode() == blockOK.hashCode())
                    block == blockOK.block && Objects.equals(tag, blockOK.tag) && (anyMeta || blockOK.damage == this.damage)
                else false
            case _ => false
        }
    }
}

case object NG extends ItemDamage {
    override val anyMeta: Boolean = false
    override val damage: Int = 0
    override val tag: NBTTagCompound = null
    override val item: Item = Item.getItemFromBlock(Blocks.AIR)

    override def equals(any: Any): Boolean = false

    override val hashCode: Int = 0

    override val toString: String = getClass.getName + " Null item @0"

    override def toStack(amount: Int): ItemStack = com.yogpc.qp.version.VersionUtil.empty()

    override val itemStackLimit = 0
}

object ItemDamage {
    def apply(itemStack: ItemStack): ItemDamage = {
        if (VersionUtil.isEmpty(itemStack)) NG
        else itemStack.getItem match {
            case block: ItemBlock => BlockOK(itemStack, block.getBlock)
            case _ => OK(itemStack)
        }
    }

    def apply(item: Item, damage: Int): ItemDamage =
        item match {
            case null => NG
            case _ => OK(new ItemStack(item, 1, damage))
        }

    def apply(item: Item): ItemDamage = apply(item, 0)

    def apply(block: Block, damage: Int): ItemDamage =
        block match {
            case null => NG
            case _ => BlockOK(new ItemStack(block, 1, damage), block)
        }

    def apply(block: Block): ItemDamage = apply(block, 0)

    def apply(option: Option[ItemStack]): ItemDamage =
        option match {
            case Some(a) => apply(a)
            case None => NG
        }

    def invalid = NG

    import scala.language.implicitConversions

    implicit class S2D(val stack: ItemStack) extends AnyVal {
        def toID(stack: ItemStack): ItemDamage = apply(stack)
    }

    def listFromArray(array: Array[ItemStack]): List[ItemDamage] = array.filter(s => s != null && VersionUtil.nonEmpty(s)).map(apply).toList
}

