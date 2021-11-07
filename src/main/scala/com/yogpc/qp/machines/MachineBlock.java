package com.yogpc.qp.machines;

import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

@Deprecated(forRemoval = true)
public abstract class MachineBlock extends BaseEntityBlock {
    public static final BooleanProperty WORKING = QPBlock.WORKING;

    protected MachineBlock(Properties settings) {
        super(settings);
    }

    @Override
    public final RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
