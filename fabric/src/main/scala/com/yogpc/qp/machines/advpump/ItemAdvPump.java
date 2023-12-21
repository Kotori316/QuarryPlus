package com.yogpc.qp.machines.advpump;

import com.yogpc.qp.machines.EnchantableItem;
import com.yogpc.qp.machines.QPBlock;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

class ItemAdvPump extends QPBlock.QPBlockItem implements EnchantableItem {
    ItemAdvPump(QPBlock block) {
        super(block, new FabricItemSettings().fireResistant());
    }

    @Override
    public List<ItemStack> creativeTabItem() {
        List<ItemStack> stacks = new ArrayList<>();
        stacks.add(new ItemStack(this));
        {
            ItemStack stack = new ItemStack(this);
            stack.enchant(Enchantments.BLOCK_EFFICIENCY, 5);
            stack.enchant(Enchantments.UNBREAKING, 3);
            stack.enchant(Enchantments.BLOCK_FORTUNE, 3);
            stacks.add(stack);
        }
        return stacks;
    }

    @Override
    public Set<Enchantment> acceptEnchantments() {
        return Set.of(Enchantments.BLOCK_EFFICIENCY, Enchantments.UNBREAKING, Enchantments.BLOCK_FORTUNE);
    }
}
