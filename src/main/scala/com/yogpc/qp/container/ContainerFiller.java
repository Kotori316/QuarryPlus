package com.yogpc.qp.container;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.packet.TileMessage;
import com.yogpc.qp.tile.TileFiller;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerFiller extends Container {
    public static final String GUI_ID = QuarryPlus.modID + ":gui_filler";
    private final TileFiller tile;
    private static final int allSlots = TileFiller.slotCount();

    public ContainerFiller(TileFiller tile, EntityPlayer player) {
        this.tile = tile;
        int oneBox = 18;

        // Filler inventory
        for (int h = 0; h < 3; h++)
            for (int v = 0; v < 9; v++)
                this.addSlotToContainer(new SlotTile(this.tile.inventory(), v + h * 9, 8 + v * oneBox, 72 + h * oneBox));

        // Player inventory
        for (int h = 0; h < 3; h++) {
            for (int v = 0; v < 9; v++) {
                this.addSlotToContainer(new Slot(player.inventory, v + h * 9 + 9, 8 + v * oneBox, 140 + h * oneBox));
            }
        }
        for (int vertical = 0; vertical < 9; vertical++) {
            this.addSlotToContainer(new Slot(player.inventory, vertical, 8 + vertical * oneBox, 198));
        }

        if (!tile.getWorld().isRemote)
            PacketHandler.sendToClient(TileMessage.create(tile), ((EntityPlayerMP) player));
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return playerIn.getDistanceSqToCenter(tile.getPos()) <= 64;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
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
                    if (this.tile.inventory().isItemValidForSlot(i, remain)) {
                        if (!mergeItemStack(remain, i, i + allSlots, false)) {
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
