package com.yogpc.qp.machines.filler;

import java.util.Objects;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.base.SlotTile;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.packet.TileMessage;
import com.yogpc.qp.utils.Holder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class FillerContainer extends Container {
    public static final String GUI_ID = QuarryPlus.modID + ":gui_" + QuarryPlus.Names.filler;
    public static final int allSlots = FillerTile.slotCount();
    final FillerTile tile;

    public FillerContainer(int id, PlayerEntity player, BlockPos pos) {
        super(Holder.fillerContainerType(), id);
        tile = ((FillerTile) player.world.getTileEntity(pos));
        Objects.requireNonNull(tile);
        int oneBox = 18;
        for (int h = 0; h < 3; h++)
            for (int v = 0; v < 9; v++)
                this.addSlot(new SlotTile(this.tile.inventory(), v + h * 9, 8 + v * oneBox, 72 + h * oneBox));

        // Player inventory
        for (int h = 0; h < 3; h++) {
            for (int v = 0; v < 9; v++) {
                this.addSlot(new Slot(player.inventory, v + h * 9 + 9, 8 + v * oneBox, 140 + h * oneBox));
            }
        }
        for (int vertical = 0; vertical < 9; vertical++) {
            this.addSlot(new Slot(player.inventory, vertical, 8 + vertical * oneBox, 198));
        }

        if (!player.world.isRemote)
            PacketHandler.sendToClient(TileMessage.create(tile), player.world);
    }


    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return tile.inventory().isUsableByPlayer(playerIn);
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
                for (int i = 0; i < allSlots; i++) {
                    if (tile.inventory().isItemValidForSlot(i, remain)) {
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
