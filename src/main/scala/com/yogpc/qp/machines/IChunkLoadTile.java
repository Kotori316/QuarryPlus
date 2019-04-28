package com.yogpc.qp.machines;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;

public interface IChunkLoadTile {
    void requestTicket();

//    default void setTileData(ForgeChunkManager.Ticket ticket, BlockPos pos) {
//        if (ticket == null) {
//            return;
//        }
//        final NBTTagCompound tag = ticket.getModData();
//        tag.setInteger("quarryX", pos.getX());
//        tag.setInteger("quarryY", pos.getY());
//        tag.setInteger("quarryZ", pos.getZ());
//        forceChunkLoading(ticket);
//    }
//
//    void forceChunkLoading(ForgeChunkManager.Ticket ticket);
}
