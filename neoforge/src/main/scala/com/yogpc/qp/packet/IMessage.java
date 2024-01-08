package com.yogpc.qp.packet;

import com.google.common.base.CaseFormat;
import com.yogpc.qp.QuarryPlus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public interface IMessage extends CustomPacketPayload {
    @Override
    void write(FriendlyByteBuf buf);

    @Override
    default ResourceLocation id() {
        return createIdentifier(getClass());
    }

    static ResourceLocation createIdentifier(Class<?> clazz) {
        return new ResourceLocation(QuarryPlus.modID, CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, clazz.getSimpleName()));
    }
}
