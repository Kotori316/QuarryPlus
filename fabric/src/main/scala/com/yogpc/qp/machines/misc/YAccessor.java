package com.yogpc.qp.machines.misc;

import com.yogpc.qp.machines.advquarry.TileAdvQuarry;
import com.yogpc.qp.machines.quarry.TileQuarry;
import com.yogpc.qp.packet.IMessage;
import net.minecraft.world.level.block.entity.BlockEntity;
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
        else if (entity instanceof TileAdvQuarry advQuarry)
            return new AdvQuarryYAccessor(advQuarry);
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
        return new LevelMessage(quarry.getLevel(), quarry.getBlockPos(), getDigMinY());
    }

    @Override
    int getLimitTop() {
        return quarry.getArea() != null ? quarry.getArea().minY() : quarry.getBlockPos().getY();
    }
}

class AdvQuarryYAccessor extends YAccessor {
    private final TileAdvQuarry quarry;

    AdvQuarryYAccessor(TileAdvQuarry quarry) {
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
        return new LevelMessage(quarry.getLevel(), quarry.getBlockPos(), getDigMinY());
    }

    @Override
    int getLimitTop() {
        // The minY in area is the position of the lowest frame.
        return quarry.getArea() != null ? quarry.getArea().minY() : quarry.getBlockPos().getY();
    }
}
