package com.yogpc.qp.packet;

import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public interface IMessage<T extends IMessage<T>> {
    static <T extends IMessage<T>> Function<FriendlyByteBuf, T> decode(Supplier<T> supplier) {
        return buffer -> supplier.get().readFromBuffer(buffer);
    }

    T readFromBuffer(FriendlyByteBuf buffer);

    void writeToBuffer(FriendlyByteBuf buffer);

    ResourceLocation getIdentifier();
}
