package com.yogpc.qp.packet;

import net.minecraft.network.FriendlyByteBuf;

public interface IMessage {
    void write(FriendlyByteBuf buf);
}
