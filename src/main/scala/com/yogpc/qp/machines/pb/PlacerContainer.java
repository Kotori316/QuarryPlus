package com.yogpc.qp.machines.pb;

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

public class PlacerContainer extends Container {
    public static final String GUI_ID = QuarryPlus.modID + ":gui_" + QuarryPlus.Names.placer;
    @Nonnull
    /*package-private*/ final PlacerTile tile;
    private final int allSlots;

    public PlacerContainer(int id, PlayerEntity player, BlockPos pos) {
        super(Holder.placerContainerType(), id);
        this.tile = (PlacerTile) Objects.requireNonNull(player.getEntityWorld().getTileEntity(pos));
        this.allSlots = this.tile.getSizeInventory();

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                this.addSlot(new SlotTile(this.tile, j + i * 3, 62 + j * 18, 17 + i * 18));
            }
        }

        for (int k = 0; k < 3; ++k) {
            for (int i1 = 0; i1 < 9; ++i1) {
                this.addSlot(new Slot(player.inventory, i1 + k * 9 + 9, 8 + i1 * 18, 84 + k * 18));
            }
        }

        for (int l = 0; l < 9; ++l) {
            this.addSlot(new Slot(player.inventory, l, 8 + l * 18, 142));
        }

        if (!player.getEntityWorld().isRemote) {
            tile.sendPacket();
        }
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return this.tile.isUsableByPlayer(playerIn);
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
            } else if (!mergeItemStack(remain, 0, allSlots, false)) {
                // simplified because tile.isItemValidForSlot(i, remain) returns true with all i.
                return ItemStack.EMPTY;
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
