package com.yogpc.qp.machines;

import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public abstract class MachineBlock extends BaseEntityBlock {
    public static final BooleanProperty WORKING = BooleanProperty.create("working");

    protected MachineBlock(Properties settings) {
        super(settings);
    }

    @Override
    public final RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
