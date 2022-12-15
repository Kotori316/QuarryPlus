package com.yogpc.qp.machines.mini_quarry;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.EnchantableItem;
import com.yogpc.qp.machines.QPBlock;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

public final class MiniQuarryItem extends QPBlock.QPBlockItem implements EnchantableItem {
    public MiniQuarryItem(QPBlock block) {
        super(block, new Properties());
    }

    @Override
    public List<ItemStack> creativeTabItem() {
        List<ItemStack> stacks = new ArrayList<>();
        stacks.add(new ItemStack(this));
        {
            var stack = new ItemStack(this);
            stack.enchant(Enchantments.BLOCK_EFFICIENCY, 5);
            stack.enchant(Enchantments.UNBREAKING, 3);
            stacks.add(stack);
        }
        return stacks;
    }

    @Override
    public Set<Enchantment> acceptEnchantments() {
        // return Set.of(Enchantments.BLOCK_EFFICIENCY, Enchantments.UNBREAKING);
        return QuarryPlus.config.acceptableEnchantmentsMap.getAllowedEnchantments(getRegistryName());
    }
}
