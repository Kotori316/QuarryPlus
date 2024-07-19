package com.yogpc.qp.machine.quarry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

public abstract class QuarryItem extends BlockItem {
    protected QuarryItem(Block block) {
        super(block, new Properties().fireResistant());
    }
}
