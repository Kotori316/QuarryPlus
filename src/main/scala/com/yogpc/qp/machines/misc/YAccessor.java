package com.yogpc.qp.machines.misc;

import com.yogpc.qp.machines.miningwell.MiningWellTile;
import com.yogpc.qp.machines.quarry.TileQuarry;
import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.packet.LevelMessage;
import javax.annotation.Nullable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

public abstract class YAccessor {
    public abstract int getDigMinY();

    public abstract void setDigMinY(int newValue);

    abstract IMessage makeMessage();

    abstract int getLimitTop();

    abstract boolean stillValid(Player player);

    @Nullable
    public static YAccessor get(@Nullable BlockEntity entity) {
        if (entity instanceof TileQuarry quarry)
            return new QuarryYAccessor(quarry);
        else if (entity instanceof MiningWellTile miningWell)
            return new MiningWellYAccessor(miningWell);
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
    public int getDigMinY() {
        return quarry.digMinY;
    }

    @Override
    public void setDigMinY(int newValue) {
        quarry.digMinY = newValue;
        quarry.setChanged();
    }

    @Override
    IMessage makeMessage() {
        return new LevelMessage(quarry.getLevel(), quarry.getBlockPos(), getDigMinY());
    }

    @Override
    int getLimitTop() {
        return quarry.getArea() != null ? quarry.getArea().minY() : quarry.getBlockPos().getY();
    }

    @Override
    boolean stillValid(Player player) {
        return quarry.stillValid(player);
    }
}

class MiningWellYAccessor extends YAccessor {
    private final MiningWellTile miningWell;

    MiningWellYAccessor(MiningWellTile miningWell) {
        this.miningWell = miningWell;
    }

    @Override
    public int getDigMinY() {
        return miningWell.digMinY;
    }

    @Override
    public void setDigMinY(int newValue) {
        miningWell.digMinY = newValue;
        miningWell.setChanged();
    }

    @Override
    IMessage makeMessage() {
        return new LevelMessage(miningWell.getLevel(), miningWell.getBlockPos(), getDigMinY());
    }

    @Override
    int getLimitTop() {
        return miningWell.getBlockPos().getY();
    }

    @Override
    boolean stillValid(Player player) {
        return miningWell.stillValid(player);
    }
}