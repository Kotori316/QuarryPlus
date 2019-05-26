package com.yogpc.qp.machines.advpump;

import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.packet.advpump.AdvPumpStatusMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerAdvPump extends Container {
    private final TileAdvPump tile;

    public ContainerAdvPump(TileAdvPump tile, EntityPlayer player) {
        this.tile = tile;
        int oneBox = 18;
        for (int h = 0; h < 3; h++) {
            for (int v = 0; v < 9; v++) {
                this.addSlot(new Slot(player.inventory, v + h * 9 + 9, 8 + v * oneBox, 84 + h * oneBox));
            }
        }
        for (int vertical = 0; vertical < 9; vertical++) {
            this.addSlot(new Slot(player.inventory, vertical, 8 + vertical * oneBox, 142));
        }
        if (!player.world.isRemote)
            PacketHandler.sendToClient(AdvPumpStatusMessage.create(tile), player.world);
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return playerIn.world.getTileEntity(tile.getPos()) == tile;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        return ItemStack.EMPTY;
    }

}
