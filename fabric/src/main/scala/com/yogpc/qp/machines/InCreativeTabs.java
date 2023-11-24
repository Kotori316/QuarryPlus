package com.yogpc.qp.machines;

import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public interface InCreativeTabs {

    default List<ItemStack> creativeTabItem() {
        return List.of(new ItemStack((ItemLike) this));
    }
}
