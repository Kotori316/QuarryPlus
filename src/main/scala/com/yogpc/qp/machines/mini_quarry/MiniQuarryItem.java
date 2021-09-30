package com.yogpc.qp.machines.mini_quarry;

import java.util.Set;

import com.yogpc.qp.Holder;
import com.yogpc.qp.machines.EnchantableItem;
import com.yogpc.qp.machines.QPBlock;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

public final class MiniQuarryItem extends QPBlock.QPBlockItem implements EnchantableItem {
    public MiniQuarryItem(QPBlock block) {
        super(block, new Properties().tab(Holder.TAB));
    }

    @Override
    public Set<Enchantment> acceptEnchantments() {
        return Set.of(Enchantments.BLOCK_EFFICIENCY, Enchantments.UNBREAKING);
    }
}
