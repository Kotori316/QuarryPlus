package com.yogpc.qp.machines.miningwell;

import com.yogpc.qp.machines.EnchantableItem;
import com.yogpc.qp.machines.QPBlock;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

import java.util.Set;

final class MiningWellItem extends QPBlock.QPBlockItem implements EnchantableItem {
    MiningWellItem(QPBlock block) {
        super(block, new Item.Properties());
    }

    @Override
    public Set<Enchantment> acceptEnchantments() {
        // Not configurable
        return Set.of(Enchantments.BLOCK_EFFICIENCY);
    }
}
