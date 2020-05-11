package com.yogpc.qp.machines.base

import java.util.function.Predicate

import com.yogpc.qp.Config
import com.yogpc.qp.machines.bookmover.BlockBookMover
import net.minecraft.block.Block
import net.minecraft.enchantment.Enchantment
import net.minecraft.item.{BlockItem, BlockItemUseContext, Item, ItemStack}
import net.minecraft.util.ActionResultType

abstract class BlockItemEnchantable(b: Block, prop: Item.Properties) extends BlockItem(b, prop) with IEnchantableItem {

  def tester(is: ItemStack): Predicate[Enchantment]

  override def canMove(is: ItemStack, enchantment: Enchantment): Boolean = {
    val stack = if (Config.common.disabled(BlockBookMover.SYMBOL).get()) is else new ItemStack(is.getItem.asItem(), is.getCount)
    tester(stack).test(enchantment)
  }

  override def isBookEnchantable(s1: ItemStack, s2: ItemStack) = false

  override def tryPlace(context: BlockItemUseContext): ActionResultType = {
    if (Option(context.getPlayer).exists(_.isCreative)) {
      val size = context.getItem.getCount
      val result = super.tryPlace(context)
      context.getItem.setCount(size)
      result
    } else {
      super.tryPlace(context)
    }
  }
}
