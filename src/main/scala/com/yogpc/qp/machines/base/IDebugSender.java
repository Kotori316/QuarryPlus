package com.yogpc.qp.machines.base;

import java.util.Collections;
import java.util.List;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

/**
 * Must be subclass of {@link APacketTile} or override {@link IDebugSender#getMessage()}.
 */
public interface IDebugSender {
    default void sendDebugMessage(PlayerEntity player) {
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
            return Collections.singletonList(new StringTextComponent(this.getClass().getSimpleName() + " is disabled."));
        } else {
            return getDebugMessages();
        }
    }

    /**
     * For internal use only. Called when machine is valid.
     *
     * @return debug info of valid machine.
     */
    List<? extends ITextComponent> getDebugMessages();
}
