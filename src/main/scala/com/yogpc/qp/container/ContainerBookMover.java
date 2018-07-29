package com.yogpc.qp.container;

import com.yogpc.qp.tile.TileBookMover;
import com.yogpc.qp.version.VersionUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerBookMover extends Container {

    private final TileBookMover mover;

    public ContainerBookMover(TileBookMover mover, EntityPlayer player) {
        this.mover = mover;

        addSlotToContainer(new SlotTile(mover, 0, 13, 35));
        addSlotToContainer(new SlotTile(mover, 1, 55, 35));
        addSlotToContainer(new SlotTile(mover, 2, 116, 35));

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlotToContainer(new Slot(player.inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int k = 0; k < 9; ++k) {
            this.addSlotToContainer(new Slot(player.inventory, k, 8 + k * 18, 142));
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return mover.isUsableByPlayer(playerIn);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        return VersionUtil.empty();
        //TODO implement shift move.
    }
}
