package com.yogpc.qp.machines.mini_quarry;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.yogpc.qp.Holder;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;

final class MiniQuarryInventory extends SimpleContainer {
    public MiniQuarryInventory() {
        super(5);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return stack.is(Holder.ITEM_FUEL_MODULE_NORMAL) ||
            stack.getItem() instanceof DiggerItem;
    }

    List<ItemStack> tools() {
        return Stream.concat(
            IntStream.range(0, getContainerSize())
                .mapToObj(this::getItem)
                .filter(s -> s.getItem() instanceof DiggerItem),
            Stream.of(ItemStack.EMPTY)
        ).toList();
    }
}
