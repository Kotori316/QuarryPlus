package com.yogpc.qp.utils

import com.yogpc.qp.tile.ItemDamage

case class ItemElement(itemDamage: ItemDamage, count: Int) {
  def toStack = itemDamage.toStack(count)

  def toNBT = {
    val nbt = toStack.serializeNBT()
    nbt.removeTag("Count")
    nbt.setInteger("Count", count)
    nbt
  }

  override def toString: String = itemDamage.toString + " x" + count
}
