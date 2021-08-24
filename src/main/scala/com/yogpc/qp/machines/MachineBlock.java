package com.yogpc.qp.machines;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.state.property.BooleanProperty;

public abstract class MachineBlock extends BlockWithEntity {
    public static final BooleanProperty WORKING = BooleanProperty.of("working");

    protected MachineBlock(Settings settings) {
        super(settings);
    }

    @Override
    public final BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }
}
