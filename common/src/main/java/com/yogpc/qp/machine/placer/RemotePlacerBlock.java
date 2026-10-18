package com.yogpc.qp.machine.placer;

import com.yogpc.qp.machine.GeneralScreenHandler;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.TRIGGERED;

public final class RemotePlacerBlock extends AbstractPlacerBlock {
    public static final String NAME = "remote_placer";

    public RemotePlacerBlock() {
        super(NAME);
        registerDefaultState(getStateDefinition().any().setValue(TRIGGERED, Boolean.FALSE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(TRIGGERED);
    }

    @Override
    protected GeneralScreenHandler<?> createScreenHandler(AbstractPlacerTile placer) {
        return new GeneralScreenHandler<>(placer, PlacerContainer::createRemotePlacerContainer);
    }
}
