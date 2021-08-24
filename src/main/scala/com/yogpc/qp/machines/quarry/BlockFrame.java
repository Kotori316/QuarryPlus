package com.yogpc.qp.machines.quarry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.yogpc.qp.QuarryPlus;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ConnectingBlock;
import net.minecraft.block.Material;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.EightWayDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.apache.commons.lang3.tuple.Pair;

import static net.minecraft.state.property.Properties.DOWN;
import static net.minecraft.state.property.Properties.EAST;
import static net.minecraft.state.property.Properties.NORTH;
import static net.minecraft.state.property.Properties.SOUTH;
import static net.minecraft.state.property.Properties.UP;
import static net.minecraft.state.property.Properties.WEST;

public class BlockFrame extends Block {
    public static final String NAME = "frame";
    public static final BooleanProperty DAMMING = BooleanProperty.of("damming");
    public static final VoxelShape BOX_AABB = VoxelShapes.cuboid(0.25, 0.25, 0.25, 0.75, 0.75, 0.75);
    public static final VoxelShape North_AABB = VoxelShapes.cuboid(0.25, 0.25, 0, 0.75, 0.75, 0.25);
    public static final VoxelShape South_AABB = VoxelShapes.cuboid(0.25, 0.25, .75, 0.75, 0.75, 1);
    public static final VoxelShape West_AABB = VoxelShapes.cuboid(0, 0.25, 0.25, .25, 0.75, 0.75);
    public static final VoxelShape East_AABB = VoxelShapes.cuboid(.75, 0.25, 0.25, 1, 0.75, 0.75);
    public static final VoxelShape UP_AABB = VoxelShapes.cuboid(0.25, .75, 0.25, 0.75, 1, 0.75);
    public static final VoxelShape Down_AABB = VoxelShapes.cuboid(0.25, 0, 0.25, 0.75, .25, 0.75);
    private static final Map<BooleanProperty, VoxelShape> SHAPE_MAP = Stream.of(
        Pair.of(NORTH, North_AABB),
        Pair.of(SOUTH, South_AABB),
        Pair.of(WEST, West_AABB),
        Pair.of(EAST, East_AABB),
        Pair.of(UP, UP_AABB),
        Pair.of(DOWN, Down_AABB)
    ).collect(Collectors.toMap(Pair::getKey, Pair::getValue));

    private static final BiPredicate<World, BlockPos> HAS_NEIGHBOUR_LIQUID = (world, pos) ->
        Stream.of(Direction.values()).map(pos::offset)
            .anyMatch(p -> !world.getFluidState(p).isEmpty());
    public final BlockItem blockItem = new BlockItem(this, new Item.Settings().group(QuarryPlus.CREATIVE_TAB));

    public BlockFrame() {
        super(Settings.of(Material.GLASS).strength(0.5f).dropsNothing());
        this.setDefaultState(this.getStateManager().getDefaultState()
            .with(NORTH, false).with(EAST, false).with(SOUTH, false)
            .with(WEST, false).with(UP, false).with(DOWN, false)
            .with(DAMMING, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(NORTH, EAST, SOUTH, WEST, UP, DOWN, DAMMING);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext context) {
        World worldIn = context.getWorld();
        BlockPos pos = context.getBlockPos();
        return this.getDefaultState()
            .with(NORTH, canConnectTo(worldIn, pos.north()))
            .with(EAST, canConnectTo(worldIn, pos.east()))
            .with(SOUTH, canConnectTo(worldIn, pos.south()))
            .with(WEST, canConnectTo(worldIn, pos.west()))
            .with(DOWN, canConnectTo(worldIn, pos.down()))
            .with(UP, canConnectTo(worldIn, pos.up()));
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos currentPos, BlockPos neighborPos) {
        return state.with(ConnectingBlock.FACING_PROPERTIES.get(direction), canConnectTo(world, currentPos.offset(direction)));
    }

    private boolean breaking = false;

    @Override
    @SuppressWarnings("deprecation")
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            boolean firstBreak;
            if (breaking)
                firstBreak = false;
            else {
                firstBreak = true;
                breaking = true;
            }
            if (firstBreak) {
                if (!HAS_NEIGHBOUR_LIQUID.test(world, pos)) {
                    breakChain(world, pos);
                }
                breaking = false;
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @SuppressWarnings("unchecked")
    private void breakChain(World world, BlockPos first) {
        if (!world.isClient) {
            Set<BlockPos> set = new HashSet<>();
            set.add(first);
            ArrayList<BlockPos> nextCheck = new ArrayList<>();
            nextCheck.add(first);
            while (!nextCheck.isEmpty()) {
                List<BlockPos> list = (List<BlockPos>) nextCheck.clone();
                nextCheck.clear();
                for (BlockPos pos : list) {
                    for (Direction8 dir : Direction8.DIRECTIONS) {
                        BlockPos nPos = pos.add(dir.vec());
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

    private boolean canConnectTo(BlockView worldIn, BlockPos pos) {
        return worldIn.getBlockState(pos).getBlock() == this;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction) {
        return true;
    }

    public BlockState getDammingState() {
        return getDefaultState().with(DAMMING, true);
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE_MAP.entrySet().stream()
            .filter(e -> state.get(e.getKey()))
            .map(Map.Entry::getValue)
            .reduce(BOX_AABB, VoxelShapes::union);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        super.neighborUpdate(state, world, pos, block, fromPos, notify);
        if (state.get(DAMMING)) {
            world.setBlockState(pos, state.with(DAMMING, HAS_NEIGHBOUR_LIQUID.test(world, pos)), 2);
        }
    }

    record Direction8(Vec3i vec) {

        public static final List<Direction8> DIRECTIONS;

        static {
            DIRECTIONS = Arrays.stream(EightWayDirection.values())
                .flatMap(d -> d.getDirections().stream().map(Direction::getVector).reduce(Vec3i::add).stream())
                .map(Direction8::new)
                .toList();
        }
    }
}