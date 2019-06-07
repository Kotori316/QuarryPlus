package com.yogpc.qp.utils

import cats.kernel.Eq
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound

case class ItemElement(itemDamage: ItemDamage, count: Int) {
  def toStack: ItemStack = itemDamage.toStack(count)

  def toNBT: NBTTagCompound = {
    val nbt = toStack.serializeNBT()
    nbt.remove("Count")
    nbt.putInt("Count", count)
    nbt
  }

  override def toString: String = itemDamage.toString + " x" + count
}

object ItemElement {
  def apply(stack: ItemStack): ItemElement = new ItemElement(ItemDamage(stack), stack.getCount)

  implicit val eq: Eq[ItemElement] = Eq.fromUniversalEquals

  val invalid: ItemElement = ItemElement(ItemDamage.invalid, 0)
}
