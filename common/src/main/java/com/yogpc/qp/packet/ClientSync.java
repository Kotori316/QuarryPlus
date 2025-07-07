package com.yogpc.qp.packet;

import com.yogpc.qp.PlatformAccess;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public interface ClientSync {
    void fromClientTag(ValueInput input);

    ValueOutput toClientTag(ValueOutput output);

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
