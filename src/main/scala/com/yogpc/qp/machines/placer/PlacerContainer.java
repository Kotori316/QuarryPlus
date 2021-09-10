package com.yogpc.qp.machines.placer;

import java.util.Objects;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.misc.SlotContainer;
import javax.annotation.Nonnull;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class PlacerContainer extends AbstractContainerMenu {
    public static final String GUI_ID = QuarryPlus.modID + ":gui_" + PlacerBlock.NAME;
    @Nonnull
    /*package-private*/ final PlacerTile tile;
    private final int allSlots;

    public PlacerContainer(int id, Player player, BlockPos pos) {
        super(Holder.PLACER_MENU_TYPE, id);
        this.tile = (PlacerTile) Objects.requireNonNull(player.level.getBlockEntity(pos));
        this.allSlots = this.tile.getContainerSize();

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                this.addSlot(new SlotContainer(this.tile, j + i * 3, 62 + j * 18, 17 + i * 18));
            }
        }

        for (int k = 0; k < 3; ++k) {
            for (int i1 = 0; i1 < 9; ++i1) {
                this.addSlot(new Slot(player.getInventory(), i1 + k * 9 + 9, 8 + i1 * 18, 84 + k * 18));
            }
        }

        for (int l = 0; l < 9; ++l) {
            this.addSlot(new Slot(player.getInventory(), l, 8 + l * 18, 142));
        }

        if (!player.level.isClientSide) {
            tile.sendPacket();
        }
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return this.tile.stillValid(playerIn);
    }

    @Override
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
