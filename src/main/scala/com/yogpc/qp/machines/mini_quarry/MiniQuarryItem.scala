package com.yogpc.qp.machines.mini_quarry

import java.util.function.Predicate

import com.yogpc.qp.machines.base.{BlockItemEnchantable, IEnchantableItem}
import net.minecraft.block.Block
import net.minecraft.enchantment.Enchantment
import net.minecraft.item.{Item, ItemStack}

class MiniQuarryItem(b: Block, prop: Item.Properties) extends BlockItemEnchantable(b, prop) {
  override def tester(is: ItemStack): Predicate[Enchantment] =
    IEnchantableItem.EFFICIENCY or IEnchantableItem.UNBREAKING
}
