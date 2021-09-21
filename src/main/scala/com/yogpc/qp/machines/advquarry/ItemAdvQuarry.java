package com.yogpc.qp.machines.advquarry;

import java.util.Set;

import com.yogpc.qp.Holder;
import com.yogpc.qp.machines.EnchantableItem;
import com.yogpc.qp.machines.QPBlock;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

class ItemAdvQuarry extends QPBlock.QPBlockItem implements EnchantableItem {
    ItemAdvQuarry(QPBlock block) {
        super(block, new Properties().tab(Holder.TAB).fireResistant());
    }

    @Override
    public Set<Enchantment> acceptEnchantments() {
        return Set.of(Enchantments.BLOCK_EFFICIENCY, Enchantments.UNBREAKING, Enchantments.BLOCK_FORTUNE, Enchantments.SILK_TOUCH);
    }
}
