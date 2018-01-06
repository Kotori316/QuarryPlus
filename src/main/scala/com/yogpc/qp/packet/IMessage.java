package com.yogpc.qp.packet;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import scala.Function0;

public interface IMessage extends net.minecraftforge.fml.common.network.simpleimpl.IMessage {

    @Override
    default void fromBytes(ByteBuf buf) {
        try {
            fromBytes(new PacketBuffer(buf));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    default void toBytes(ByteBuf buf) {
        toBytes(new PacketBuffer(buf));
    }

    void fromBytes(PacketBuffer buffer) throws IOException;

    void toBytes(PacketBuffer buffer);

    IMessage onRecieve(IMessage message, MessageContext ctx);

    static <T> Runnable toRunnable(Function0<T> function) {
        return function::apply;
    }
}
