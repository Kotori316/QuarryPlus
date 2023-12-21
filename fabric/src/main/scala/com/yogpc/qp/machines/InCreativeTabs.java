package com.yogpc.qp.machines;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.List;

public interface InCreativeTabs {

    default List<ItemStack> creativeTabItem() {
        return List.of(new ItemStack((ItemLike) this));
    }
}
