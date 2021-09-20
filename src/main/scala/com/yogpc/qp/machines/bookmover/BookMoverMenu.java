package com.yogpc.qp.machines.bookmover;

import com.yogpc.qp.Holder;
import com.yogpc.qp.machines.misc.SlotContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BookMoverMenu extends AbstractContainerMenu {

    private final BookMoverEntity mover;
    private final DataSlot progress = this.addDataSlot(DataSlot.standalone());
    private final DataSlot isWorking = this.addDataSlot(DataSlot.standalone());

    public BookMoverMenu(int id, Player player, BlockPos pos) {
        super(Holder.BOOK_MOVER_MENU_TYPE, id);
        this.mover = (BookMoverEntity) player.level.getBlockEntity(pos);

        if (mover != null) {
            addSlot(new SlotContainer(mover, 0, 13, 35));
            addSlot(new SlotContainer(mover, 1, 55, 35));
            addSlot(new SlotContainer(mover, 2, 116, 35));
            if (!player.level.isClientSide) {
                this.setTrackValues();
            }
        }

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(player.getInventory(), j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(player.getInventory(), k, 8 + k * 18, 142));
        }
    }

    private void setTrackValues() {
        this.progress.set(1000 * mover.getEnergyStored() / mover.getMaxEnergyStored());
        this.isWorking.set(mover.isWorking() ? 1 : 0);
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return mover.stillValid(playerIn);
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    public ItemStack quickMoveStack(Player player, int index) {
        int allSlots = 3;
        Slot slot = this.getSlot(index);
        if (slot.hasItem()) {
            ItemStack remain = slot.getItem();
            ItemStack slotContent = remain.copy();
            if (index < allSlots) {
                if (!this.moveItemStackTo(remain, allSlots, 36 + allSlots, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(remain, 0, allSlots, false)) {
                return ItemStack.EMPTY;
            }

            if (remain.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (remain.getCount() == slotContent.getCount()) {
                // Nothing moved
                return ItemStack.EMPTY;
            }

            slot.onTake(player, remain);
            return slotContent;
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public void broadcastChanges() {
        setTrackValues();
        super.broadcastChanges();
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
