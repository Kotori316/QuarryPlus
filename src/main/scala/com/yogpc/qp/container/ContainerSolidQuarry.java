package com.yogpc.qp.container;

import java.util.Objects;

import com.yogpc.qp.tile.TileSolidQuarry;
import com.yogpc.qp.version.VersionUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerSolidQuarry extends Container {
    private final TileSolidQuarry quarry;

    public ContainerSolidQuarry(TileSolidQuarry quarry, EntityPlayer player) {
        this.quarry = quarry;

        addSlotToContainer(new SlotTile(quarry, 0, 44, 27));

        int oneBox = 18;
        for (int h = 0; h < 3; h++) {
            for (int v = 0; v < 9; v++) {
                this.addSlotToContainer(new Slot(player.inventory, v + h * 9 + 9, 8 + v * oneBox, 84 + h * oneBox));
            }
        }
        for (int vertical = 0; vertical < 9; vertical++) {
            this.addSlotToContainer(new Slot(player.inventory, vertical, 8 + vertical * oneBox, 142));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return quarry.isUsableByPlayer(playerIn);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        int allSlots = 1;
        ItemStack src = VersionUtil.empty();
        final Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            final ItemStack remain = slot.getStack();
            src = Objects.requireNonNull(remain).copy();
            if (index < allSlots) {
                if (!mergeItemStack(remain, allSlots, allSlots + 36, true))
                    return VersionUtil.empty();
            } else {
                for (int i = 0; i < allSlots; i++) {
                    if (quarry.isItemValidForSlot(i, remain)) {
                        if (!mergeItemStack(remain, i, i + 1, false)) {
                            return VersionUtil.empty();
                        }
                        break;
                    }
                }
            }
            if (VersionUtil.getCount(remain) == 0)
                slot.putStack(VersionUtil.empty());
            else
                slot.onSlotChanged();
            if (VersionUtil.getCount(remain) == VersionUtil.getCount(src))
                return VersionUtil.empty();
            VersionUtil.onTake(slot, playerIn, remain);
        }
        return src;
    }
}
