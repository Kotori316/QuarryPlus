package com.yogpc.qp.tile;

import net.minecraftforge.common.ForgeChunkManager;

public interface IChunkLoadTile {
    void requestTicket();

    void forceChunkLoading(ForgeChunkManager.Ticket ticket);
}
