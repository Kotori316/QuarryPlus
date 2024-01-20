package com.yogpc.qp.machines.misc;

import com.yogpc.qp.machines.PowerTile;
import com.yogpc.qp.machines.advquarry.TileAdvQuarry;
import com.yogpc.qp.machines.miningwell.MiningWellTile;
import com.yogpc.qp.machines.quarry.TileQuarry;
import com.yogpc.qp.packet.ClientSync;
import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.packet.LevelMessage;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public abstract class YAccessor<T extends BlockEntity & ClientSync> {
    public abstract int getDigMinY();

    public abstract void setDigMinY(int newValue);

    abstract IMessage makeMessage();

    abstract int getLimitTop();

    abstract boolean stillValid(Player player);

    abstract T getEntity();

    @Nullable
    public static YAccessor<?> get(@Nullable BlockEntity entity) {
        if (entity instanceof TileQuarry quarry)
            return new QuarryYAccessor(quarry);
        else if (entity instanceof MiningWellTile miningWell)
            return new MiningWellYAccessor(miningWell);
        else if (entity instanceof TileAdvQuarry quarry)
            return new AdvQuarryYAccessor(quarry);
        else
            return null;
    }
}

class QuarryYAccessor extends YAccessor<TileQuarry> {
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
        return PowerTile.stillValid(quarry, player);
    }

    @Override
    TileQuarry getEntity() {
        return quarry;
    }
}

class MiningWellYAccessor extends YAccessor<MiningWellTile> {
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
        return PowerTile.stillValid(miningWell, player);
    }

    @Override
    MiningWellTile getEntity() {
        return miningWell;
    }
}

class AdvQuarryYAccessor extends YAccessor<TileAdvQuarry> {
    private final TileAdvQuarry quarry;

    AdvQuarryYAccessor(TileAdvQuarry quarry) {
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
        return PowerTile.stillValid(quarry, player);
    }

    @Override
    TileAdvQuarry getEntity() {
        return quarry;
    }
}
