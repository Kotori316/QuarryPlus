package com.yogpc.qp.machine.placer;

import com.yogpc.qp.machine.GeneralScreenHandler;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.TRIGGERED;

public final class PlacerBlock extends AbstractPlacerBlock {
    public static final String NAME = "placer_plus";

    public PlacerBlock() {
        super(NAME);
        registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH).setValue(TRIGGERED, Boolean.FALSE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, TRIGGERED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(FACING, ctx.getNearestLookingDirection().getOpposite());
    }

    @Override
    protected GeneralScreenHandler<?> createScreenHandler(AbstractPlacerTile placer) {
        return new GeneralScreenHandler<>(placer, PlacerContainer::createPlacerContainer);
    }
}
