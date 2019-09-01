package com.yogpc.qp.machines.quarry;

import com.yogpc.qp.machines.base.SlotTile;
import com.yogpc.qp.utils.Holder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class ContainerSolidQuarry extends Container {
    private final TileSolidQuarry quarry;

    public ContainerSolidQuarry(int id, PlayerEntity player, BlockPos pos) {
        super(Holder.solidQuarryContainerType(), id);
        this.quarry = ((TileSolidQuarry) player.getEntityWorld().getTileEntity(pos));

        addSlot(new SlotTile(quarry, 0, 44, 27));

        int oneBox = 18;
        for (int h = 0; h < 3; h++) {
            for (int v = 0; v < 9; v++) {
                this.addSlot(new Slot(player.inventory, v + h * 9 + 9, 8 + v * oneBox, 84 + h * oneBox));
            }
        }
        for (int vertical = 0; vertical < 9; vertical++) {
            this.addSlot(new Slot(player.inventory, vertical, 8 + vertical * oneBox, 142));
        }
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return quarry.isUsableByPlayer(playerIn);
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
        int allSlots = 1;
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
                    if (quarry.isItemValidForSlot(i, remain)) {
                        if (!mergeItemStack(remain, i, i + 1, false)) {
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
