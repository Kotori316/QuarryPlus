package com.yogpc.qp.item

import net.minecraft.block.Block
import net.minecraft.enchantment.{Enchantment, EnchantmentHelper}
import net.minecraft.init.Enchantments
import net.minecraft.item.{ItemBlock, ItemStack}

class ItemBlockEnchantable(block: Block) extends ItemBlock(block) with IEnchantableItem {

    override def canMove(is: ItemStack, enchantment: Enchantment): Boolean = {
        if (EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, is) > 0) {
            enchantment != Enchantments.FORTUNE && enchantment == Enchantments.UNBREAKING || enchantment == Enchantments.EFFICIENCY || enchantment == Enchantments.SILK_TOUCH
        } else if (EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, is) > 0) {
            enchantment != Enchantments.SILK_TOUCH && enchantment == Enchantments.UNBREAKING || enchantment == Enchantments.EFFICIENCY || enchantment == Enchantments.FORTUNE
        } else {
            enchantment == Enchantments.SILK_TOUCH || enchantment == Enchantments.FORTUNE || enchantment == Enchantments.UNBREAKING || enchantment == Enchantments.EFFICIENCY
        }
    }

    override def isBookEnchantable(itemstack1: ItemStack, itemstack2: ItemStack) = false
}