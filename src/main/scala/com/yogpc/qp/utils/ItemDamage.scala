package com.yogpc.qp.utils

import java.util.Objects

import net.minecraft.block.{Block, Blocks}
import net.minecraft.item.{BlockItem, Item, ItemStack}
import net.minecraft.nbt.CompoundNBT

sealed abstract class ItemDamage extends Ordered[ItemDamage] {
  val item: Item
  val tag: CompoundNBT

  def equals(any: Any): Boolean

  def hashCode(): Int

  def toStack(amount: Int = 1): ItemStack = {
    val s = new ItemStack(item, amount)
    if (tag != null) {
      s.setTag(tag.copy())
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
  val tag: CompoundNBT = itemStack.getTag

  override def toString: String = item.getTranslationKey

  override def equals(any: Any): Boolean = {
    any match {
      case itemDamage: OK =>
        if (hashCode() == itemDamage.hashCode())
          if (Objects.equals(tag, itemDamage.tag))
            return item == itemDamage.item
        false
      case _ => false
    }
  }

  override def hashCode(): Int = item.hashCode

  override def toStack(amount: Int): ItemStack = {
    val a = itemStack.copy()
    a.setCount(amount)
    a
  }

  override def itemStackLimit: Int = item.getItemStackLimit(itemStack)
}

case class BlockOK(itemStack: ItemStack, block: Block) extends ItemDamage {
  val item: Item = block.asItem()
  val tag = itemStack.getTag

  override def toString: String = block.getTranslationKey

  override def hashCode(): Int = block.hashCode

  override def equals(any: Any): Boolean = {
    any match {
      case blockOK: BlockOK =>
        if (hashCode() == blockOK.hashCode())
          block == blockOK.block && Objects.equals(tag, blockOK.tag)
        else false
      case _ => false
    }
  }
}

case object NG extends ItemDamage {
  override val tag: CompoundNBT = null
  override val item: Item = Blocks.AIR.asItem()

  override def equals(any: Any): Boolean = false

  override val hashCode: Int = 0

  override val toString: String = getClass.getName + " Null item @0"

  override def toStack(amount: Int): ItemStack = ItemStack.EMPTY

  override val itemStackLimit = 0
}

object ItemDamage {
  def apply(itemStack: ItemStack): ItemDamage = {
    if (itemStack.isEmpty) NG
    else itemStack.getItem match {
      case block: BlockItem => BlockOK(itemStack, block.getBlock)
      case _ => OK(itemStack)
    }
  }

  def apply(item: Item): ItemDamage =
    item match {
      case null => NG
      case _ => OK(new ItemStack(item, 1))
    }

  def apply(block: Block, damage: Int): ItemDamage =
    block match {
      case null => NG
      case _ => BlockOK(new ItemStack(block, 1), block)
    }

  def apply(block: Block): ItemDamage = apply(block, 0)

  def apply(option: Option[ItemStack]): ItemDamage =
    option match {
      case Some(a) => apply(a)
      case None => NG
    }

  def invalid = NG

}

