package com.yogpc.qp.machines.advpump

import java.util.function.Predicate

import com.yogpc.qp.machines.base.BlockItemEnchantable
import com.yogpc.qp.machines.base.IEnchantableItem._
import net.minecraft.block.Block
import net.minecraft.enchantment.Enchantment
import net.minecraft.item.{Item, ItemStack}

class BlockItemAdvPump(b: Block, prop: Item.Properties) extends BlockItemEnchantable(b, prop) {

  override def tester(is: ItemStack): Predicate[Enchantment] = SILKTOUCH or FORTUNE or UNBREAKING or EFFICIENCY
}
