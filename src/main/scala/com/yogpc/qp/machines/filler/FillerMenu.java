package com.yogpc.qp.machines.filler;

import java.util.Objects;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.misc.SlotContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class FillerMenu extends AbstractContainerMenu {
    public static final String GUI_ID = QuarryPlus.modID + ":gui_" + FillerBlock.NAME;
    @NotNull
    final FillerEntity filler;

    public FillerMenu(int id, Player player, BlockPos pos) {
        super(QuarryPlus.ModObjects.FILLER_MENU_TYPE, id);
        this.filler = (FillerEntity) Objects.requireNonNull(player.level.getBlockEntity(pos));
        int oneBox = 18;

        // Filler inventory
        for (int h = 0; h < 3; h++)
            for (int v = 0; v < 9; v++)
                this.addSlot(new SlotContainer(this.filler.container, v + h * 9, 8 + v * oneBox, 72 + h * oneBox));

        // Player inventory
        for (int h = 0; h < 3; h++) {
            for (int v = 0; v < 9; v++) {
                this.addSlot(new Slot(player.getInventory(), v + h * 9 + 9, 8 + v * oneBox, 140 + h * oneBox));
            }
        }
        for (int vertical = 0; vertical < 9; vertical++) {
            this.addSlot(new Slot(player.getInventory(), vertical, 8 + vertical * oneBox, 198));
        }
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return filler.getBlockPos().distSqr(pPlayer.position(), true) < 64;
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    public ItemStack quickMoveStack(Player player, int index) {
        int allSlots = filler.container.getContainerSize();
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
}
