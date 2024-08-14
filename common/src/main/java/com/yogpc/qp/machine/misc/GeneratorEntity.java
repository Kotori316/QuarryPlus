package com.yogpc.qp.machine.misc;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.machine.PowerEntity;
import com.yogpc.qp.machine.QpBlock;
import com.yogpc.qp.machine.QpEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class GeneratorEntity extends QpEntity {
    public GeneratorEntity(BlockPos pos, BlockState blockState) {
        super(PlatformAccess.getAccess().registerObjects().getBlockEntityType((QpBlock) blockState.getBlock()).orElseThrow(),
            pos, blockState);
    }

    static void serverTick(Level level, BlockPos pos, BlockState state, GeneratorEntity e) {
        if (!e.enabled) {
            return;
        }
        var powerLevel = state.getValue(BlockStateProperties.LEVEL);
        var power = GeneratorBlock.ENERGY[powerLevel];
        var mutablePos = new BlockPos.MutableBlockPos();

        for (var value : Direction.values()) {
            var entity = level.getBlockEntity(mutablePos.setWithOffset(pos, value));
            if (entity instanceof PowerEntity powerEntity) {
                powerEntity.addEnergy(power, false);
            }
        }
    }
}
