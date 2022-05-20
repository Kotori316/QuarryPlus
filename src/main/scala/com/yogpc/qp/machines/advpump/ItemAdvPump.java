package com.yogpc.qp.machines.advpump;

import java.util.Set;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.EnchantableItem;
import com.yogpc.qp.machines.QPBlock;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

class ItemAdvPump extends QPBlock.QPBlockItem implements EnchantableItem {
    ItemAdvPump(QPBlock block) {
        super(block, new FabricItemSettings().tab(QuarryPlus.ModObjects.CREATIVE_TAB).fireResistant());
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> stacks) {
        super.fillItemCategory(group, stacks);
        if (this.allowedIn(group)) {
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
