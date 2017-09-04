package com.yogpc.qp.packet.workbench;

import java.io.IOException;

import com.yogpc.qp.packet.IMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * TODO create
 * To client only.
 */
public class WorkbenchMessage implements IMessage {

    @Override
    public void fromBytes(PacketBuffer buffer) throws IOException {

    }

    @Override
    public void toBytes(PacketBuffer buffer) {

    }

    @Override
    public IMessage onRecieve(IMessage message, MessageContext ctx) {
        return null;
    }
}
