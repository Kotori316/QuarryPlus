package com.yogpc.qp.machine.misc;

import com.yogpc.qp.machine.QpBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.*;

public class FrameBlock extends QpBlock {
    public static final String NAME = "frame";
    public static final BooleanProperty DAMMING = BooleanProperty.create("damming");
    public static final VoxelShape BOX_AABB = Shapes.box(0.125, 0.125, 0.125, 0.875, 0.875, 0.875);
    private static final BiPredicate<Level, BlockPos> HAS_NEIGHBOUR_LIQUID = (world, pos) ->
        Stream.of(Direction.values()).map(pos::relative)
            .anyMatch(p -> !world.getFluidState(p).isEmpty());

    public FrameBlock() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.NONE).strength(0.5f).noLootTable(), NAME, b -> new BlockItem(b, new Item.Properties()));
        this.registerDefaultState(getStateDefinition().any()
            .setValue(NORTH, false).setValue(EAST, false).setValue(SOUTH, false)
            .setValue(WEST, false).setValue(UP, false).setValue(DOWN, false)
            .setValue(DAMMING, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN, DAMMING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        var worldIn = context.getLevel();
        var pos = context.getClickedPos();
        return this.defaultBlockState()
            .setValue(NORTH, canConnectTo(worldIn, pos.north()))
            .setValue(EAST, canConnectTo(worldIn, pos.east()))
            .setValue(SOUTH, canConnectTo(worldIn, pos.south()))
            .setValue(WEST, canConnectTo(worldIn, pos.west()))
            .setValue(DOWN, canConnectTo(worldIn, pos.below()))
            .setValue(UP, canConnectTo(worldIn, pos.above()));
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos currentPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
        return state.setValue(PipeBlock.PROPERTY_BY_DIRECTION.get(direction), canConnectTo(levelReader, currentPos.relative(direction)));
    }

    private boolean breaking = false;

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            if (!breaking) {
                breaking = true;
                if (!HAS_NEIGHBOUR_LIQUID.test(world, pos)) {
                    breakChain(world, pos);
                }
                breaking = false;
            }
            super.onRemove(state, world, pos, newState, moved);
        }
    }

    private void breakChain(Level world, BlockPos first) {
        if (!world.isClientSide) {
            Set<BlockPos> set = new HashSet<>();
            set.add(first);
            ArrayList<BlockPos> current = new ArrayList<>();
            current.add(first);
            while (!current.isEmpty()) {
                ArrayList<BlockPos> nextCheck = new ArrayList<>();
                for (BlockPos pos : current) {
                    for (Direction26 dir : Direction26.DIRECTIONS) {
                        BlockPos nPos = pos.offset(dir.vec());
                        BlockState nBlock = world.getBlockState(nPos);
                        if (nBlock.getBlock() == this) {
                            if (!HAS_NEIGHBOUR_LIQUID.test(world, nPos) && set.add(nPos))
                                nextCheck.add(nPos);
                        }
                    }
                }
                current = nextCheck;
            }
            set.forEach(pos -> world.removeBlock(pos, false));
        }
    }

    private boolean canConnectTo(BlockGetter worldIn, BlockPos pos) {
        return worldIn.getBlockState(pos).is(this);
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState stateFrom, Direction direction) {
        return true;
    }

    public BlockState getDammingState() {
        return defaultBlockState().setValue(DAMMING, true);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return BOX_AABB;
    }

    @Override
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, Orientation orientation, boolean notify) {
        super.neighborChanged(state, world, pos, block, orientation, notify);
        if (state.getValue(DAMMING)) {
            world.setBlock(pos, state.setValue(DAMMING, HAS_NEIGHBOUR_LIQUID.test(world, pos)), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected QpBlock createBlock(Properties properties) {
        return new FrameBlock();
    }
}
