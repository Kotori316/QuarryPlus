package com.yogpc.qp.packet;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public interface OnReceiveWithLevel {
    void onReceive(Level level, Player player);
}
