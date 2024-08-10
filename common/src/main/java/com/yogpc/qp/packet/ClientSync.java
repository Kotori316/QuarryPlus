package com.yogpc.qp.packet;

import com.yogpc.qp.PlatformAccess;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface ClientSync {
    void fromClientTag(CompoundTag tag, HolderLookup.Provider registries);

    CompoundTag toClientTag(CompoundTag tag, HolderLookup.Provider registries);

    @SuppressWarnings("unchecked") // Checked. Safe
    default <T extends BlockEntity & ClientSync> void syncToClient() {
        if (this instanceof BlockEntity entity) {
            var level = entity.getLevel();
            if (level != null && !level.isClientSide) {
                var clientSyncMessage = new ClientSyncMessage((T) this);
                PlatformAccess.getAccess().packetHandler().sendToClientWorld(clientSyncMessage, level);
            }
        }
    }
}
