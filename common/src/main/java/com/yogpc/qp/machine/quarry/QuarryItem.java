package com.yogpc.qp.machine.quarry;

import com.yogpc.qp.machine.QpBlock;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;

public abstract class QuarryItem extends BlockItem {
    protected QuarryItem(QpBlock block) {
        super(block, new Properties().fireResistant().setId(ResourceKey.create(Registries.ITEM, block.name)));
    }
}
