package com.yogpc.qp.machines.misc;

import java.util.Arrays;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.PowerTile;
import com.yogpc.qp.utils.MapMulti;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class CreativeGeneratorTile extends PowerTile {
    private long sendEnergy = ONE_FE * 1000L;

    public CreativeGeneratorTile(BlockPos pos, BlockState state) {
        super(QuarryPlus.ModObjects.CREATIVE_GENERATOR_TYPE, pos, state);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putLong("sendEnergy", sendEnergy);
        return super.writeNbt(nbt);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        sendEnergy = nbt.getLong("sendEnergy");
    }

    static final BlockEntityTicker<CreativeGeneratorTile> TICKER = (world, pos, state, tile) ->
        Arrays.stream(Direction.values())
            .map(pos::offset)
            .map(world::getBlockEntity)
            .mapMulti(MapMulti.cast(PowerTile.class))
            .forEach(t -> t.addEnergy(tile.sendEnergy, false));
}
