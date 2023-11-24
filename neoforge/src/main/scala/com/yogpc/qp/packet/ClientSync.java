package com.yogpc.qp.packet;

import net.minecraft.nbt.CompoundTag;

public interface ClientSync {
    void fromClientTag(CompoundTag tag);

    CompoundTag toClientTag(CompoundTag tag);
}
