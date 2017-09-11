package com.yogpc.qp.item;

import com.yogpc.qp.block.BlockMiningWell;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockMiningwell extends ItemBlock implements IEnchantableItem {

    public ItemBlockMiningwell(BlockMiningWell block) {
        super(block);
    }

    @Override
    public boolean canMove(final ItemStack is, Enchantment enchantment) {
        return enchantment == Enchantments.SILK_TOUCH || enchantment == Enchantments.FORTUNE
                || enchantment == Enchantments.UNBREAKING || enchantment == Enchantments.EFFICIENCY;
    }

    @Override
    public boolean isBookEnchantable(final ItemStack itemstack1, final ItemStack itemstack2) {
        return false;
    }

}
