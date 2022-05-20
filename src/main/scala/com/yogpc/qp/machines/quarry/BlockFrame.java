package com.yogpc.qp.machines.quarry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

import com.yogpc.qp.machines.Direction8;
import com.yogpc.qp.machines.QPBlock;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.DOWN;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.EAST;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.NORTH;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.SOUTH;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.UP;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WEST;

public class BlockFrame extends QPBlock {
    public static final String NAME = "frame";
    public static final BooleanProperty DAMMING = BooleanProperty.create("damming");
    public static final VoxelShape BOX_AABB = Shapes.box(0.125, 0.125, 0.125, 0.875, 0.875, 0.875);

    private static final BiPredicate<Level, BlockPos> HAS_NEIGHBOUR_LIQUID = (world, pos) ->
        Stream.of(Direction.values()).map(pos::relative)
            .anyMatch(p -> !world.getFluidState(p).isEmpty());

    public BlockFrame() {
        super(FabricBlockSettings.of(Material.GLASS).strength(0.5f).drops(BuiltInLootTables.EMPTY), NAME);
        this.registerDefaultState(this.getStateDefinition().any()
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
        Level worldIn = context.getLevel();
        BlockPos pos = context.getClickedPos();
        return this.defaultBlockState()
            .setValue(NORTH, canConnectTo(worldIn, pos.north()))
            .setValue(EAST, canConnectTo(worldIn, pos.east()))
            .setValue(SOUTH, canConnectTo(worldIn, pos.south()))
            .setValue(WEST, canConnectTo(worldIn, pos.west()))
            .setValue(DOWN, canConnectTo(worldIn, pos.below()))
            .setValue(UP, canConnectTo(worldIn, pos.above()));
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos currentPos, BlockPos neighborPos) {
        return state.setValue(PipeBlock.PROPERTY_BY_DIRECTION.get(direction), canConnectTo(world, currentPos.relative(direction)));
    }

    private boolean breaking = false;

    @Override
    @SuppressWarnings("deprecation")
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

    @SuppressWarnings("unchecked")
    private void breakChain(Level world, BlockPos first) {
        if (!world.isClientSide) {
            Set<BlockPos> set = new HashSet<>();
            set.add(first);
            ArrayList<BlockPos> nextCheck = new ArrayList<>();
            nextCheck.add(first);
            while (!nextCheck.isEmpty()) {
                List<BlockPos> list = (List<BlockPos>) nextCheck.clone();
                nextCheck.clear();
                for (BlockPos pos : list) {
                    for (Direction8 dir : Direction8.DIRECTIONS) {
                        BlockPos nPos = pos.offset(dir.vec());
                        BlockState nBlock = world.getBlockState(nPos);
                        if (nBlock.getBlock() == this) {
                            if (!HAS_NEIGHBOUR_LIQUID.test(world, nPos) && set.add(nPos))
                                nextCheck.add(nPos);
                        }
                    }
                }
            }
            set.forEach(pos -> world.removeBlock(pos, false));
        }
    }

    private boolean canConnectTo(BlockGetter worldIn, BlockPos pos) {
        return worldIn.getBlockState(pos).is(this);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean skipRendering(BlockState state, BlockState stateFrom, Direction direction) {
        return true;
    }

    public BlockState getDammingState() {
        return defaultBlockState().setValue(DAMMING, true);
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return BOX_AABB;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        super.neighborChanged(state, world, pos, block, fromPos, notify);
        if (state.getValue(DAMMING)) {
            world.setBlock(pos, state.setValue(DAMMING, HAS_NEIGHBOUR_LIQUID.test(world, pos)), 2);
        }
    }
}
