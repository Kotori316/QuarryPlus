package com.yogpc.qp.machines.advpump

import com.yogpc.qp.machines.base.IEnchantableItem._
import com.yogpc.qp.machines.base.ItemBlockEnchantable
import net.minecraft.block.Block
import net.minecraft.item.{Item, ItemStack}

class ItemBlockAdvPump(b: Block, prop: Item.Properties) extends ItemBlockEnchantable(b, prop) {

  override def tester(is: ItemStack) = SILKTOUCH or FORTUNE or UNBREAKING or EFFICIENCY
}
