package com.yogpc.qp.machines.mini_quarry;

import java.util.Objects;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.base.SlotTile;
import com.yogpc.qp.utils.Holder;
import javax.annotation.Nonnull;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class MiniQuarryContainer extends Container {
    public static final String GUI_ID = QuarryPlus.modID + ":gui_" + QuarryPlus.Names.mini_quarry;
    @Nonnull
    public final MiniQuarryTile tile;
    private final int allSlots;

    public MiniQuarryContainer(int id, PlayerEntity player, BlockPos pos) {
        super(Holder.miniQuarryContainerType(), id);
        tile = (MiniQuarryTile) Objects.requireNonNull(player.getEntityWorld().getTileEntity(pos));
        int oneBox = 18;
        this.allSlots = tile.getInv().getSizeInventory();

        for (int i = 0; i < allSlots; i++) {
            int verticalFix = i < 5 ? i : i - 5;
            int horizontalFix = i / 5;
            addSlot(new SlotTile(tile.getInv(), i, 44 + verticalFix * oneBox, 27 + horizontalFix * oneBox));
        }
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
        return true;
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
        ItemStack src = ItemStack.EMPTY;
        final Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            final ItemStack remain = slot.getStack();
            src = remain.copy();
            if (index < allSlots) {
                if (!mergeItemStack(remain, allSlots, allSlots + 36, true))
                    return ItemStack.EMPTY;
            } else {
                if (!mergeItemStack(remain, 0, allSlots, false)) {
                    return ItemStack.EMPTY;
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
