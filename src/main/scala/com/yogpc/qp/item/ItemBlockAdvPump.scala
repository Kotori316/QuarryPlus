package com.yogpc.qp.item

import net.minecraft.block.Block
import net.minecraft.enchantment.Enchantment
import net.minecraft.init.Enchantments
import net.minecraft.item.{ItemBlock, ItemStack}

class ItemBlockAdvPump(block: Block) extends ItemBlock(block) with IEnchantableItem {

    override def canMove(is: ItemStack, enchantment: Enchantment) = {
        enchantment == Enchantments.SILK_TOUCH || enchantment == Enchantments.FORTUNE || enchantment == Enchantments.UNBREAKING || enchantment == Enchantments.EFFICIENCY
    }

    override def isBookEnchantable(itemstack1: ItemStack, itemstack2: ItemStack) = false
}
