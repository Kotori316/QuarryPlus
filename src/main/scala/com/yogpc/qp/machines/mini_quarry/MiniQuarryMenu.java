package com.yogpc.qp.machines.mini_quarry;

import java.util.Objects;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.misc.SlotContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public final class MiniQuarryMenu extends AbstractContainerMenu {
    public static final String GUI_ID = QuarryPlus.modID + ":gui_" + MiniQuarryBlock.NAME;
    final MiniQuarryTile miniQuarry;
    private final int allSlots;

    public MiniQuarryMenu(int id, Player player, BlockPos pos) {
        super(Holder.MINI_QUARRY_MENU_TYPE, id);
        miniQuarry = Objects.requireNonNull((MiniQuarryTile) player.level.getBlockEntity(pos));
        this.allSlots = miniQuarry.container.getContainerSize();
        final int oneBox = 18;

        for (int i = 0; i < allSlots; i++) {
            int verticalFix = i < 5 ? i : i - 5;
            int horizontalFix = i / 5;
            addSlot(new SlotContainer(miniQuarry.container, i, 44 + verticalFix * oneBox, 27 + horizontalFix * oneBox));
        }

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
        return miniQuarry.getBlockPos().distSqr(player.position(), true) < 64;
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
