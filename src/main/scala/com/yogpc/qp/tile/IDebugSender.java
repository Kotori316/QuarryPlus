package com.yogpc.qp.tile;

import java.util.List;

import com.yogpc.qp.version.VersionUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.ITextComponent;

public interface IDebugSender {
    default void sendDebugMessage(EntityPlayer player) {
        getDebugmessages().forEach(t -> VersionUtil.sendMessage(player, t));
    }

    String getDebugName();

    List<? extends ITextComponent> getDebugmessages();
}
