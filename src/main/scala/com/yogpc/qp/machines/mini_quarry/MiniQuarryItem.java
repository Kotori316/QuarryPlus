package com.yogpc.qp.machines.mini_quarry;

import java.util.Set;

import com.yogpc.qp.Holder;
import com.yogpc.qp.machines.EnchantableItem;
import com.yogpc.qp.machines.QPBlock;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

public final class MiniQuarryItem extends QPBlock.QPBlockItem implements EnchantableItem {
    public MiniQuarryItem(QPBlock block) {
        super(block, new Properties().tab(Holder.TAB));
    }

    @Override
    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> stacks) {
        super.fillItemCategory(tab, stacks);
        if (this.allowdedIn(tab)) {
            var stack = new ItemStack(this);
            stack.enchant(Enchantments.BLOCK_EFFICIENCY, 5);
            stack.enchant(Enchantments.UNBREAKING, 3);
            stacks.add(stack);
        }
    }

    @Override
    public Set<Enchantment> acceptEnchantments() {
        return Set.of(Enchantments.BLOCK_EFFICIENCY, Enchantments.UNBREAKING);
    }
}