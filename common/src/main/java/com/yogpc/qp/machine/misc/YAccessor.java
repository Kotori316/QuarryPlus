package com.yogpc.qp.machine.misc;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.machine.quarry.QuarryEntity;
import com.yogpc.qp.packet.ClientSync;
import com.yogpc.qp.packet.YSetterMessage;
import net.minecraft.world.level.block.entity.BlockEntity;

public record YAccessor<T extends BlockEntity & ClientSync>(DigMinY digMinY, T entity) {
    public static YAccessor<?> get(BlockEntity blockEntity) {
        return switch (blockEntity) {
            case QuarryEntity q -> new YAccessor<>(q.digMinY, q);
            case null, default -> null;
        };
    }

    public int getDigMinY() {
        return digMinY.getMinY(entity.getLevel());
    }

    void setDigMinY(int y) {
        digMinY.setMinY(y);
    }

    public int getLimitTop() {
        return entity.getBlockPos().getY() - 1;
    }

    public void syncToServer() {
        var message = new YSetterMessage(entity, digMinY.getMinY(entity.getLevel()));
        PlatformAccess.getAccess().packetHandler().sendToServer(message);
    }
}
