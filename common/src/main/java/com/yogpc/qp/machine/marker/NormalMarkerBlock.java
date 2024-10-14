package com.yogpc.qp.machine.marker;

import com.yogpc.qp.machine.QpBlock;
import com.yogpc.qp.machine.QpBlockItem;
import com.yogpc.qp.machine.QpEntityBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;

public class NormalMarkerBlock extends QpEntityBlock {
    private static final VoxelShape STANDING_Shape = Shapes.box(.35, 0, .35, .65, .65, .65);
    private static final VoxelShape DOWN_Shape = Shapes.box(.35, .35, .35, .65, 1, .65);
    private static final VoxelShape NORTH_Shape = Shapes.box(.35, .35, .35, .65, .65, 1);
    private static final VoxelShape SOUTH_Shape = Shapes.box(.35, .35, 0, .65, .65, .65);
    private static final VoxelShape WEST_Shape = Shapes.box(.35, .35, .35, 1, .65, .65);
    private static final VoxelShape EAST_Shape = Shapes.box(0.0D, .35, .35, .65, .65, .65);

    public static final String NAME = "marker";

    public NormalMarkerBlock() {
        super(Properties.of().mapColor(MapColor.NONE).pushReaction(PushReaction.DESTROY).instabreak().lightLevel(value -> 7).noCollission(),
            NAME, b -> new QpBlockItem(b, new Item.Properties()));
        this.registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.UP));
    }

    @Override
    protected QpBlock createBlock(Properties properties) {
        return new NormalMarkerBlock();
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
    public boolean skipRendering(BlockState state, BlockState stateFrom, Direction direction) {
        return true;
    }

    @Override
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
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        Direction direction = state.getValue(FACING);
        BlockPos blockPos = pos.relative(direction.getOpposite());
        BlockState blockState = world.getBlockState(blockPos);
        return blockState.isFaceSturdy(world, blockPos, direction);
    }

    @Override
    public BlockState updateShape(BlockState state, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos currentPos, Direction direction, BlockPos blockPos2, BlockState blockState2, RandomSource randomSource) {
        return state.canSurvive(levelReader, currentPos) ? super.updateShape(state, levelReader, scheduledTickAccess, currentPos, direction, blockPos2, blockState2, randomSource) : Blocks.AIR.defaultBlockState();
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof NormalMarkerEntity marker) {
            if (!level.isClientSide()) {
                marker.tryConnect(c -> player.displayClientMessage(c, false));
            }
            return InteractionResult.SUCCESS_SERVER;
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }
}
