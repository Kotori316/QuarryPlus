package com.yogpc.qp.machines.advpump;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.EnchantableItem;
import com.yogpc.qp.machines.QPBlock;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

class ItemAdvPump extends QPBlock.QPBlockItem implements EnchantableItem {
    ItemAdvPump(QPBlock block) {
        super(block, new Item.Properties().fireResistant());
    }

    @Override
    public List<ItemStack> creativeTabItem() {
        List<ItemStack> stacks = new ArrayList<>();
        stacks.add(new ItemStack(this));
        {
            var stack = new ItemStack(this);
            stack.enchant(Enchantments.BLOCK_EFFICIENCY, 5);
            stack.enchant(Enchantments.UNBREAKING, 3);
            stack.enchant(Enchantments.BLOCK_FORTUNE, 3);
            stacks.add(stack);
        }
        return stacks;
    }

    @Override
    public Set<Enchantment> acceptEnchantments() {
        // return Set.of(Enchantments.BLOCK_EFFICIENCY, Enchantments.UNBREAKING, Enchantments.BLOCK_FORTUNE);
        return QuarryPlus.config.acceptableEnchantmentsMap.getAllowedEnchantments(getRegistryName());
    }
}
