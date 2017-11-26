package com.yogpc.qp.tile;

import net.minecraft.entity.player.EntityPlayer;

public interface IDebugSender {
    void sendDebugMessage(EntityPlayer player);

    String getName();
}
