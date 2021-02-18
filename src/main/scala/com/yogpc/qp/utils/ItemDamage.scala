package com.yogpc.qp.utils

import cats._
import cats.implicits._
import com.google.gson.JsonObject
import com.mojang.serialization.{Dynamic, JsonOps}
import javax.annotation.Nonnull
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.nbt.{CompoundNBT, NBTDynamicOps}
import net.minecraft.util.IItemProvider
import org.openzen.zencode.java.ZenCodeType.Nullable

sealed abstract class ItemDamage {
  val item: Item
  val tag: CompoundNBT

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

  def serializeJson: JsonObject = {
    val obj = new JsonObject
    obj.addProperty("item", item.getRegistryName.toString)
    if (tag != null)
      obj.add("nbt", Dynamic.convert(NBTDynamicOps.INSTANCE, JsonOps.INSTANCE, tag))
    obj
  }
}

object ItemDamage {
  def apply(@Nonnull itemStack: ItemStack): ItemDamage = {
    if (itemStack.isEmpty) {
      if (itemStack eq ItemStack.EMPTY)
        ItemDamageNG
      else {
        val copied = {
          val before = itemStack.getCount
          itemStack.setCount(1)
          val t = itemStack.copy()
          itemStack.setCount(before)
          t
        }
        if (copied.isEmpty)
          ItemDamageNG
        else
          ItemDamageImpl(copied.getItem, copied.getTag)
      }
    } else {
      ItemDamageImpl(itemStack.getItem, itemStack.getTag)
    }
  }

  def apply(@Nullable item: IItemProvider): ItemDamage = if (item == null) ItemDamageNG else ItemDamageImpl(item.asItem(), null)

  def invalid: ItemDamage = ItemDamageNG

  private case class ItemDamageImpl(override val item: Item,
                                    override val tag: CompoundNBT) extends ItemDamage {
    override def toString = item.getTranslationKey
  }

  private case object ItemDamageNG extends ItemDamage {
    override val item: Item = null
    override val tag: CompoundNBT = null

    override def toStack(amount: Int): ItemStack = ItemStack.EMPTY

    override def itemStackLimit: Int = 64

    override def toString = "null_item"
  }

  implicit val eqItemDamage: Eq[ItemDamage] = (x: ItemDamage, y: ItemDamage) =>
    (x.## === y.##) && x.item == y.item && x.tag == y.tag

  implicit val defaultOrder: Ordering[ItemDamage] = (x: ItemDamage, y: ItemDamage) =>
    // Item.getIdFromItem can accepts null.
    Integer.compare(Item.getIdFromItem(x.item), Item.getIdFromItem(y.item))
}
