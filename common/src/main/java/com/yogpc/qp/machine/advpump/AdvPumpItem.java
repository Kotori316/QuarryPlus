package com.yogpc.qp.machine.advpump;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

public class AdvPumpItem extends BlockItem {
    public AdvPumpItem(Block block) {
        super(block, new Properties().fireResistant());
    }
}
