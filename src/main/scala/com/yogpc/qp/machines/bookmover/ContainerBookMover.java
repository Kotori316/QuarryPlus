package com.yogpc.qp.machines.bookmover;

import com.yogpc.qp.machines.base.SlotTile;
import com.yogpc.qp.utils.Holder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IntReferenceHolder;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ContainerBookMover extends Container {

    private final TileBookMover mover;
    private final IntReferenceHolder progress = this.trackInt(IntReferenceHolder.single());
    private final IntReferenceHolder isWorking = this.trackInt(IntReferenceHolder.single());

    public ContainerBookMover(int id, PlayerEntity player, BlockPos pos) {
        super(Holder.bookMoverContainerType(), id);
        this.mover = (TileBookMover) player.getEntityWorld().getTileEntity(pos);

        if (mover != null) {
            addSlot(new SlotTile(mover, 0, 13, 35));
            addSlot(new SlotTile(mover, 1, 55, 35));
            addSlot(new SlotTile(mover, 2, 116, 35));
            if (!player.getEntityWorld().isRemote) {
                this.progress.set((int) (mover.getStoredEnergy() / mover.getMaxStored() * 1000));
                this.isWorking.set(mover.isWorking() ? 1 : 0);
            }
        }

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(player.inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(player.inventory, k, 8 + k * 18, 142));
        }
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return mover.isUsableByPlayer(playerIn);
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
        int allSlots = 3;
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
                    if (mover.isItemValidForSlot(i, remain)) {
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

    @Override
    public void detectAndSendChanges() {
        int progress = (int) (mover.getStoredEnergy() / mover.getMaxStored() * 1000);
        this.progress.set(progress);
        this.isWorking.set(mover.isWorking() ? 1 : 0);
        super.detectAndSendChanges();
    }

    @OnlyIn(Dist.CLIENT)
    public int getProgress() {
        return progress.get();
    }

    @OnlyIn(Dist.CLIENT)
    public boolean moverIsWorking() {
        return this.isWorking.get() == 1;
    }
}
