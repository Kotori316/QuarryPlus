package com.yogpc.qp.machines.misc;

import java.util.Arrays;
import java.util.stream.IntStream;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.PowerTile;
import com.yogpc.qp.utils.MapMulti;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;

public class CreativeGeneratorTile extends PowerTile {
    private long sendEnergy = 0;
    private int magnification = 1;

    public CreativeGeneratorTile(BlockPos pos, BlockState state) {
        super(QuarryPlus.ModObjects.CREATIVE_GENERATOR_TYPE, pos, state);
        setSendEnergy(ONE_FE * QuarryPlus.config.power.creativeGeneratorGeneration);
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        nbt.putLong("sendEnergy", sendEnergy);
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        setSendEnergy(nbt.getLong("sendEnergy"));
    }

    public void setSendEnergy(long sendEnergy) {
        this.sendEnergy = sendEnergy;
        this.maxEnergy = sendEnergy;
        this.energy = sendEnergy;
    }

    public void setMagnification(int magnification) {
        this.magnification = magnification;
        this.setSendEnergy(ONE_FE * QuarryPlus.config.power.creativeGeneratorGeneration * magnification);
    }

    public void cycleMagnification() {
        IntStream.of(1, 5, 10, 20, 50, 100)
            .filter(i -> i > magnification)
            .findFirst()
            .ifPresentOrElse(this::setMagnification, () -> this.setMagnification(1));
    }

    public long getSendEnergy() {
        return sendEnergy;
    }

    static final BlockEntityTicker<CreativeGeneratorTile> TICKER = (world, pos, state, tile) ->
        Arrays.stream(Direction.values())
            .map(pos::relative)
            .map(world::getBlockEntity)
            .mapMulti(MapMulti.cast(PowerTile.class))
            .forEach(t -> t.addEnergy(tile.sendEnergy, false));
}
