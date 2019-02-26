package com.yogpc.qp.container;

import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.packet.quarry.LevelMessage;
import com.yogpc.qp.tile.TileQuarry;
import com.yogpc.qp.version.VersionUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerQuarryLevel extends Container {
    private final TileQuarry tile;

    public ContainerQuarryLevel(TileQuarry tile, EntityPlayer player) {
        this.tile = tile;
        int oneBox = 18;
        for (int h = 0; h < 3; h++) {
            for (int v = 0; v < 9; v++) {
                this.addSlotToContainer(new Slot(player.inventory, v + h * 9 + 9, 8 + v * oneBox, 84 + h * oneBox));
            }
        }
        for (int vertical = 0; vertical < 9; vertical++) {
            this.addSlotToContainer(new Slot(player.inventory, vertical, 8 + vertical * oneBox, 142));
        }
        if (!tile.getWorld().isRemote) {
            PacketHandler.sendToClient(LevelMessage.create(tile), (EntityPlayerMP) player);
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return tile.getWorld().getTileEntity(tile.getPos()) == tile;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        return VersionUtil.empty();
    }
}
