package com.yogpc.qp.item

import com.yogpc.qp.item.IEnchantableItem._
import net.minecraft.block.Block
import net.minecraft.enchantment.Enchantment
import net.minecraft.item.{ItemBlock, ItemStack}

class ItemBlockAdvPump(block: Block) extends ItemBlock(block) with IEnchantableItem {

    override def canMove(is: ItemStack, enchantment: Enchantment) = {
        SILKTOUCH.or(FORTUNE).or(UNBREAKING).or(EFFICIENCY).test(enchantment)
    }

    override def isBookEnchantable(itemstack1: ItemStack, itemstack2: ItemStack) = false
}
