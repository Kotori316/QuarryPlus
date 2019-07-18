package com.yogpc.qp.container;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.tile.QuarryModuleInventory;
import com.yogpc.qp.tile.TileQuarry2;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerQuarryModule extends Container {
    public static final String GUI_ID = QuarryPlus.modID + ":gui_quarry_module";
    private final QuarryModuleInventory moduleInventory;
    private final int allSlots;

    public ContainerQuarryModule(TileQuarry2 quarry, EntityPlayer player) {
        this.moduleInventory = quarry.moduleInv();
        this.allSlots = moduleInventory.getSizeInventory();
        int oneBox = 18;

        for (int i = 0; i < allSlots; i++) {
            addSlotToContainer(new SlotTile(moduleInventory, i, 44 + i * oneBox, 27));
        }

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
        return moduleInventory.isUsableByPlayer(playerIn);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack src = ItemStack.EMPTY;
        final Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            final ItemStack remain = slot.getStack();
            src = remain.copy();
            if (index < allSlots) {
                if (!mergeItemStack(remain, allSlots, allSlots + 36, true))
                    return ItemStack.EMPTY;
            } else {
                for (int i = 0; i < allSlots; i++) {
                    if (moduleInventory.isItemValidForSlot(i, remain)) {
                        if (!mergeItemStack(remain, i, i + allSlots, false)) {
                            return ItemStack.EMPTY;
                        }
                        break;
                    }
                }
            }
            if (remain.getCount() == 0)
                slot.putStack(ItemStack.EMPTY);
            else
                slot.onSlotChanged();
            if (remain.getCount() == src.getCount())
                return ItemStack.EMPTY;
            slot.onTake(playerIn, remain);
        }
        return src;
    }
}
