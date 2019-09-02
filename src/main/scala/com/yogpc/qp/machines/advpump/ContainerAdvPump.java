package com.yogpc.qp.machines.advpump;

import java.util.Objects;

import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.packet.advpump.AdvPumpStatusMessage;
import com.yogpc.qp.utils.Holder;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class ContainerAdvPump extends Container {
    public final TileAdvPump tile;

    public ContainerAdvPump(int id, PlayerEntity player, BlockPos pos) {
        super(Holder.advPumpContainerType(), id);
        this.tile = ((TileAdvPump) player.getEntityWorld().getTileEntity(pos));
        Objects.requireNonNull(this.tile);
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
    public boolean canInteractWith(PlayerEntity playerIn) {
        return playerIn.world.getTileEntity(tile.getPos()) == tile;
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
        return ItemStack.EMPTY;
    }

}
