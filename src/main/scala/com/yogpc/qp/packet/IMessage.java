package com.yogpc.qp.packet;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public interface IMessage extends net.minecraftforge.fml.common.network.simpleimpl.IMessage {

    @Override
    public default void fromBytes(ByteBuf buf) {
        try {
            fromBytes(new PacketBuffer(buf));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public default void toBytes(ByteBuf buf) {
        toBytes(new PacketBuffer(buf));
    }

    public void fromBytes(PacketBuffer buffer) throws IOException;

    public void toBytes(PacketBuffer buffer);

    IMessage onReceive(IMessage message, MessageContext ctx);
}
