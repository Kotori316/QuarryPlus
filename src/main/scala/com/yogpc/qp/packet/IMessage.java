package com.yogpc.qp.packet;

import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public interface IMessage<T extends IMessage<T>> {
    static <T extends IMessage<T>> Function<PacketByteBuf, T> decode(Supplier<T> supplier) {
        return buffer -> supplier.get().readFromBuffer(buffer);
    }

    T readFromBuffer(PacketByteBuf buffer);

    void writeToBuffer(PacketByteBuf buffer);

    Identifier getIdentifier();
}
