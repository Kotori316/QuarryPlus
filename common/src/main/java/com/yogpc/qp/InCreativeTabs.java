package com.yogpc.qp;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.stream.Stream;

public interface InCreativeTabs {

    default Stream<ItemStack> creativeTabItem() {
        return Stream.of(new ItemStack((ItemLike) this));
    }
}
