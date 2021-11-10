package com.yogpc.qp.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public interface IMessage<T extends IMessage<T>> {
    void writeToBuffer(FriendlyByteBuf buffer);

    ResourceLocation getIdentifier();
}
