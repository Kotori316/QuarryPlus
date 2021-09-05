package com.yogpc.qp.machines.misc;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SlotContainer extends Slot {
    public SlotContainer(Container container, int slot, int x, int y) {
        super(container,slot,x,y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return container.canPlaceItem(getContainerSlot(), stack);
    }
}
