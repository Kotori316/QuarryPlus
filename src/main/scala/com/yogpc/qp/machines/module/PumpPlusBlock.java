package com.yogpc.qp.machines.module;

import com.yogpc.qp.machines.QPBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public class PumpPlusBlock extends QPBlock implements QuarryModuleProvider.Block {
    public static final String NAME = "pump_plus";

    public PumpPlusBlock() {
        super(BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL)
            .pushReaction(PushReaction.BLOCK)
            .strength(3.0f), NAME);
    }

    @Override
    public QuarryModule getModule(Level level, BlockPos pos, BlockState state) {
        return QuarryModule.Constant.PUMP;
    }

}
