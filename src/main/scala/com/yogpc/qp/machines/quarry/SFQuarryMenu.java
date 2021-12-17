package com.yogpc.qp.machines.quarry;

import java.util.Objects;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.misc.SlotContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class SFQuarryMenu extends AbstractContainerMenu {
    public static final String GUI_ID = QuarryPlus.modID + ":gui_" + SFQuarryBlock.NAME;
    final DataSlot fuelCountData = this.addDataSlot(DataSlot.standalone());
    @NotNull
    SFQuarryEntity quarry;
    private final int allSlots;

    public SFQuarryMenu(int id, Player player, BlockPos pos) {
        super(Holder.SOLID_FUEL_QUARRY_MENU_TYPE, id);
        quarry = Objects.requireNonNull((SFQuarryEntity) player.level.getBlockEntity(pos));
        allSlots = quarry.fuelContainer.getContainerSize();
        addSlot(new SlotContainer(quarry.fuelContainer, 0, 44, 27));

        int oneBox = 18;
        for (int h = 0; h < 3; h++) {
            for (int v = 0; v < 9; v++) {
                this.addSlot(new Slot(player.getInventory(), v + h * 9 + 9, 8 + v * oneBox, 84 + h * oneBox));
            }
        }
        for (int vertical = 0; vertical < 9; vertical++) {
            this.addSlot(new Slot(player.getInventory(), vertical, 8 + vertical * oneBox, 142));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return quarry.enabled;
    }

    @Override
    public void broadcastChanges() {
        fuelCountData.set(Math.min(quarry.fuelCount, Short.MAX_VALUE));
        super.broadcastChanges();
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    public ItemStack quickMoveStack(Player player, int index) {
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
                // Nothing is moved
                return ItemStack.EMPTY;
            }
            slot.onTake(player, remain);
            return slotContent;
        } else {
            return ItemStack.EMPTY;
        }
    }
}
