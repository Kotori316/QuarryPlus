package com.yogpc.qp.machine;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;

public class QpBlockItem extends BlockItem {
    public QpBlockItem(QpBlock block, Properties properties) {
        super(block, properties.setId(ResourceKey.create(Registries.ITEM, block.name)));
    }
}
