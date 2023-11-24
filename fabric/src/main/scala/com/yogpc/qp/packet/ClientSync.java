package com.yogpc.qp.packet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface ClientSync {
    void fromClientTag(CompoundTag tag);

    CompoundTag toClientTag(CompoundTag tag);

    default void sync() {
        if (this instanceof BlockEntity entity) {
            var level = entity.getLevel();
            if (level != null && !level.isClientSide) {
                var clientSyncMessage = new ClientSyncMessage(entity.getBlockPos(), level.dimension(), toClientTag(new CompoundTag()));
                PacketHandler.sendToClientWorld(clientSyncMessage, level);
            }
        }
    }
}
