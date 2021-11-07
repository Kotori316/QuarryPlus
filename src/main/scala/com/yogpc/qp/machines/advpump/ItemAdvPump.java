package com.yogpc.qp.machines.advpump;

import java.util.Set;

import com.yogpc.qp.machines.EnchantableItem;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;

class ItemAdvPump extends BlockItem implements EnchantableItem {
    ItemAdvPump(Block block, Properties settings) {
        super(block, settings);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return stack.getCount() == 1;
    }

    @Override
    public int getEnchantmentValue() {
        return 25;
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> stacks) {
        super.fillItemCategory(group, stacks);
        if (this.allowdedIn(group)) {
            ItemStack stack = new ItemStack(this);
            stack.enchant(Enchantments.BLOCK_EFFICIENCY, 5);
            stack.enchant(Enchantments.UNBREAKING, 3);
            stack.enchant(Enchantments.BLOCK_FORTUNE, 3);
            stacks.add(stack);
        }
    }

    @Override
    public Set<Enchantment> acceptEnchantments() {
        return Set.of(Enchantments.BLOCK_EFFICIENCY, Enchantments.UNBREAKING, Enchantments.BLOCK_FORTUNE);
    }
}
