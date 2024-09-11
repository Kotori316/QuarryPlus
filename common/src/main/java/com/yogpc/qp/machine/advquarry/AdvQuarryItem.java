package com.yogpc.qp.machine.advquarry;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

public class AdvQuarryItem extends BlockItem {
    public AdvQuarryItem(Block block) {
        super(block, new Properties().fireResistant());
    }
}
