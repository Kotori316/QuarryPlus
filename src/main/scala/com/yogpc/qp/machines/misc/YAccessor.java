package com.yogpc.qp.machines.misc;

import com.yogpc.qp.machines.quarry.TileQuarry;
import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.packet.LevelMessage;
import net.minecraft.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

abstract class YAccessor {
    abstract int getDigMinY();

    abstract void setDigMinY(int newValue);

    abstract IMessage<?> makeMessage();

    abstract int getLimitTop();

    @Nullable
    static YAccessor get(BlockEntity entity) {
        if (entity instanceof TileQuarry quarry)
            return new QuarryYAccessor(quarry);
        else
            return null;
    }
}

class QuarryYAccessor extends YAccessor {
    private final TileQuarry quarry;

    QuarryYAccessor(TileQuarry quarry) {
        this.quarry = quarry;
    }

    @Override
    int getDigMinY() {
        return quarry.digMinY;
    }

    @Override
    void setDigMinY(int newValue) {
        quarry.digMinY = newValue;
    }

    @Override
    IMessage<?> makeMessage() {
        return LevelMessage.create(quarry.getWorld(), quarry.getPos(), getDigMinY());
    }

    @Override
    int getLimitTop() {
        return quarry.getArea() != null ? quarry.getArea().minY() : quarry.getPos().getY();
    }
}
