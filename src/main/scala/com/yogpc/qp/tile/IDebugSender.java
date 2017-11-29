package com.yogpc.qp.tile;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.ITextComponent;

public interface IDebugSender {
    default void sendDebugMessage(EntityPlayer player) {
        getDebugmessages().forEach(t -> player.sendStatusMessage(t, false));
    }

    String getName();

    List<? extends ITextComponent> getDebugmessages();
}
