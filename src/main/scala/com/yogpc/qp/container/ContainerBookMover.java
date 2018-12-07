package com.yogpc.qp.container;

import com.yogpc.qp.tile.TileBookMover;
import com.yogpc.qp.version.VersionUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ContainerBookMover extends Container {

    private final TileBookMover mover;
    private int progress;

    public ContainerBookMover(TileBookMover mover, EntityPlayer player) {
        this.mover = mover;

        addSlotToContainer(new SlotTile(mover, 0, 13, 35));
        addSlotToContainer(new SlotTile(mover, 1, 55, 35));
        addSlotToContainer(new SlotTile(mover, 2, 116, 35));

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlotToContainer(new Slot(player.inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int k = 0; k < 9; ++k) {
            this.addSlotToContainer(new Slot(player.inventory, k, 8 + k * 18, 142));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return mover.isUsableByPlayer(playerIn);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        int allSlots = 3;
        ItemStack src = VersionUtil.empty();
        final Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            final ItemStack remain = slot.getStack();
            src = remain.copy();
            if (index < allSlots) {
                if (!mergeItemStack(remain, allSlots, allSlots + 36, true))
                    return VersionUtil.empty();
            } else {
                for (int i = 0; i < allSlots; i++) {
                    if (mover.isItemValidForSlot(i, remain)) {
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

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        int progress = (int) (mover.getStoredEnergy() / mover.getMaxStored() * 1000);
        listeners.forEach(listener -> listener.sendWindowProperty(this, 0, progress));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int data) {
        super.updateProgressBar(id, data);
        if (id == 0) {
            this.progress = data;
        }
    }

    @SideOnly(Side.CLIENT)
    public int getProgress() {
        return progress;
    }
}
