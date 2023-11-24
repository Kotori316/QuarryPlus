package com.yogpc.qp.machines.marker;

import com.yogpc.qp.Holder;
import com.yogpc.qp.machines.QPBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;

public class BlockMarker extends QPBlock implements EntityBlock {
    private static final VoxelShape STANDING_Shape = Shapes.box(.35, 0, .35, .65, .65, .65);
    private static final VoxelShape DOWN_Shape = Shapes.box(.35, .35, .35, .65, 1, .65);
    private static final VoxelShape NORTH_Shape = Shapes.box(.35, .35, .35, .65, .65, 1);
    private static final VoxelShape SOUTH_Shape = Shapes.box(.35, .35, 0, .65, .65, .65);
    private static final VoxelShape WEST_Shape = Shapes.box(.35, .35, .35, 1, .65, .65);
    private static final VoxelShape EAST_Shape = Shapes.box(0.0D, .35, .35, .65, .65, .65);

    public static final String NAME = "marker";

    public BlockMarker() {
        super(Properties.of().mapColor(MapColor.NONE).pushReaction(PushReaction.DESTROY).instabreak().lightLevel(value -> 7).noCollission(), NAME);
        this.registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.UP));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return defaultBlockState().setValue(FACING, ctx.getClickedFace());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return Holder.MARKER_TYPE.create(pos, state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!world.isClientSide) {
            if (world.getBlockEntity(pos) instanceof TileMarker marker) {
                marker.tryConnect(true);
                marker.getArea().ifPresent(area ->
                    player.displayClientMessage(Component.literal("%sMarker Area%s: %s".formatted(ChatFormatting.AQUA, ChatFormatting.RESET, area)), false));
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean skipRendering(BlockState state, BlockState stateFrom, Direction direction) {
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case UP -> STANDING_Shape;
            case DOWN -> DOWN_Shape;
            case EAST -> EAST_Shape;
            case WEST -> WEST_Shape;
            case NORTH -> NORTH_Shape;
            case SOUTH -> SOUTH_Shape;
        };
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        Direction direction = state.getValue(FACING);
        BlockPos blockPos = pos.relative(direction.getOpposite());
        BlockState blockState = world.getBlockState(blockPos);
        return blockState.isFaceSturdy(world, blockPos, direction);
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos currentPos, BlockPos neighborPos) {
        return state.canSurvive(world, currentPos) ? state : Blocks.AIR.defaultBlockState();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        super.neighborChanged(state, world, pos, block, fromPos, notify);
        if (!world.isClientSide) {
            if (world.getBlockEntity(pos) instanceof TileMarker marker) {
                marker.rsReceiving = world.hasNeighborSignal(pos);
                marker.sync();
            }
        }
    }
}
