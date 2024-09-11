package com.yogpc.qp.machine.advquarry;

import com.yogpc.qp.machine.QpBlock;
import com.yogpc.qp.machine.QpEntityBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import static com.yogpc.qp.machine.QpBlockProperty.WORKING;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;

public class AdvQuarryBlock extends QpEntityBlock {
    public static final String NAME = "adv_quarry";

    public AdvQuarryBlock() {
        super(Properties.of()
            .mapColor(MapColor.METAL)
            .pushReaction(PushReaction.BLOCK)
            .strength(1.5f, 10f)
            .sound(SoundType.STONE), NAME, AdvQuarryItem::new);
        registerDefaultState(getStateDefinition().any()
            .setValue(WORKING, false)
            .setValue(FACING, Direction.NORTH));
    }

    @Override
    protected QpBlock createBlock(Properties properties) {
        return new AdvQuarryBlock();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(WORKING, FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction facing = ctx.getPlayer() == null ? Direction.NORTH : ctx.getPlayer().getDirection().getOpposite();
        return defaultBlockState().setValue(FACING, facing);
    }

}
