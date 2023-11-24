package com.yogpc.qp.machines.misc;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SlotContainer extends Slot {
    private final boolean canTake;

    public SlotContainer(Container container, int slot, int x, int y, boolean canTake) {
        super(container, slot, x, y);
        this.canTake = canTake;
    }

    public SlotContainer(Container container, int slot, int x, int y) {
        this(container, slot, x, y, true);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return container.canPlaceItem(getContainerSlot(), stack);
    }

    @Override
    public boolean mayPickup(Player pPlayer) {
        return canTake;
    }
}
