package com.yogpc.qp.utils

import cats._
import cats.implicits._
import net.minecraft.item.ItemStack
import net.minecraft.nbt.CompoundNBT

case class ItemElement(itemDamage: ItemDamage, count: Long) {
  def toStack: ItemStack = itemDamage.toStack(ProxyCommon.toInt(count))

  def toNBT: CompoundNBT = {
    val nbt = toStack.serializeNBT()
    nbt.remove("Count")
    nbt.putLong("Count", count)
    nbt
  }

  override def toString: String = itemDamage.toString + " x" + count

  def +(that: ItemElement): ItemElement = {
    if (that === ItemElement.invalid) this
    else if (this === ItemElement.invalid) that
    else {
      if (this.itemDamage =!= that.itemDamage) {
        throw new IllegalArgumentException(s"Tries to combine different kind of items. $this, $that")
      } else {
        ItemElement(this.itemDamage, this.count + that.count)
      }
    }
  }
}

object ItemElement {
  def apply(stack: ItemStack): ItemElement =
    if (!stack.isEmpty) new ItemElement(ItemDamage(stack), stack.getCount)
    else invalid // No reasons to combine empty items. Its result should be also empty.

  implicit val eqItemElement: Eq[ItemElement] = Eq.fromUniversalEquals
  implicit val showItemElement: Show[ItemElement] = Show.fromToString

  val invalid: ItemElement = ItemElement(ItemDamage.invalid, 0)
}
