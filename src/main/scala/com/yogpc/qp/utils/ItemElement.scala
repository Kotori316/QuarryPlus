package com.yogpc.qp.utils

import com.yogpc.qp.tile.ItemDamage
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound

case class ItemElement(itemDamage: ItemDamage, count: Int) {
  def toStack: ItemStack = itemDamage.toStack(count)

  def toNBT: NBTTagCompound = {
    val nbt = toStack.serializeNBT()
    nbt.removeTag("Count")
    nbt.setInteger("Count", count)
    nbt
  }

  override def toString: String = itemDamage.toString + " x" + count

  def +(that: ItemElement): ItemElement = {
    if (that == ItemElement.invalid) this
    else if (this == ItemElement.invalid) that
    else {
      if (this.itemDamage != that.itemDamage) {
        throw new IllegalArgumentException(s"Tries to combine different kind of items. $this, $that")
      } else {
        ItemElement(this.itemDamage, this.count + that.count)
      }
    }
  }
}
