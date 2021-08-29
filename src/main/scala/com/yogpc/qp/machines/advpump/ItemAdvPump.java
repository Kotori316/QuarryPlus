package com.yogpc.qp.machines.advpump;

import java.util.Set;

import com.yogpc.qp.machines.EnchantableItem;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

class ItemAdvPump extends BlockItem implements EnchantableItem {
    ItemAdvPump(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return stack.getCount() == 1;
    }

    @Override
    public int getEnchantability() {
        return 25;
    }

    @Override
    public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
        super.appendStacks(group, stacks);
        if (this.isIn(group)) {
            var stack = new ItemStack(this);
            stack.addEnchantment(Enchantments.EFFICIENCY, 5);
            stack.addEnchantment(Enchantments.UNBREAKING, 3);
            stack.addEnchantment(Enchantments.FORTUNE, 3);
            stacks.add(stack);
        }
    }

    @Override
    public Set<Enchantment> acceptEnchantments() {
        return Set.of(Enchantments.EFFICIENCY, Enchantments.UNBREAKING, Enchantments.FORTUNE);
    }
}
