package com.yogpc.qp.machines.base

import java.util.function.Predicate

import com.yogpc.qp.Config
import com.yogpc.qp.machines.bookmover.BlockBookMover
import net.minecraft.block.Block
import net.minecraft.enchantment.Enchantment
import net.minecraft.item.{BlockItemUseContext, Item, ItemBlock, ItemStack}

abstract class ItemBlockEnchantable(b: Block, prop: Item.Properties) extends ItemBlock(b, prop) with IEnchantableItem {

  def tester(is: ItemStack): Predicate[Enchantment]

  override def canMove(is: ItemStack, enchantment: Enchantment) = {
    val stack = if (Config.common.disabled(BlockBookMover.SYMBOL).get()) is else new ItemStack(is.getItem.asItem(), is.getCount)
    tester(stack).test(enchantment)
  }

  override def isBookEnchantable(s1: ItemStack, s2: ItemStack) = false

  override def tryPlace(context: BlockItemUseContext) = {
    if (Option(context.getPlayer).exists(_.abilities.isCreativeMode)) {
      val size = context.getItem.getCount
      val result = super.tryPlace(context)
      context.getItem.setCount(size)
      result
    } else {
      super.tryPlace(context)
    }
  }
}
