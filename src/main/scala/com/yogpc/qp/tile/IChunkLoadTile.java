package com.yogpc.qp.tile;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.ForgeChunkManager;

public interface IChunkLoadTile {
    void requestTicket();

    default void setTileData(ForgeChunkManager.Ticket ticket, BlockPos pos) {
        if (ticket == null) {
            return;
        }
        final NBTTagCompound tag = ticket.getModData();
        tag.setInteger("quarryX", pos.getX());
        tag.setInteger("quarryY", pos.getY());
        tag.setInteger("quarryZ", pos.getZ());
        forceChunkLoading(ticket);
    }

    void forceChunkLoading(ForgeChunkManager.Ticket ticket);
}
