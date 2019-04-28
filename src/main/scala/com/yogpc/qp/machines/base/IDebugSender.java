package com.yogpc.qp.machines.base;

import java.util.Collections;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;

public interface IDebugSender {
    default void sendDebugMessage(EntityPlayer player) {
        getMessage().forEach(t -> player.sendStatusMessage(t, false));
    }

    String getDebugName();

    /**
     * Use this instead of {@link IDebugSender#getDebugMessages()} to get info. This method consider whether the machine is disabled.
     *
     * @return debug info.
     */
    default List<? extends ITextComponent> getMessage() {
        if (((APacketTile) this).machineDisabled) {
            return Collections.singletonList(new TextComponentString(((APacketTile) this).getSymbol().name() + " is disabled."));
        } else {
            return getDebugMessages();
        }
    }

    /**
     * For internal use only.
     *
     * @return debug info of valid machine.
     */
    List<? extends ITextComponent> getDebugMessages();
}
