package com.yogpc.qp.machines.marker;

import com.yogpc.qp.QuarryPlus;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

import static net.minecraft.state.property.Properties.FACING;

public class BlockMarker extends BlockWithEntity {
    private static final VoxelShape STANDING_Shape = VoxelShapes.cuboid(.35, 0, .35, .65, .65, .65);
    private static final VoxelShape DOWN_Shape = VoxelShapes.cuboid(.35, .35, .35, .65, 1, .65);
    private static final VoxelShape NORTH_Shape = VoxelShapes.cuboid(.35, .35, .35, .65, .65, 1);
    private static final VoxelShape SOUTH_Shape = VoxelShapes.cuboid(.35, .35, 0, .65, .65, .65);
    private static final VoxelShape WEST_Shape = VoxelShapes.cuboid(.35, .35, .35, 1, .65, .65);
    private static final VoxelShape EAST_Shape = VoxelShapes.cuboid(0.0D, .35, .35, .65, .65, .65);

    public static final String NAME = "marker";
    public final BlockItem blockItem = new BlockItem(this, new Item.Settings().group(QuarryPlus.CREATIVE_TAB));

    public BlockMarker() {
        super(Settings.of(Material.DECORATION).luminance(value -> 7).noCollision());
        setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.UP));
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(FACING);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return getDefaultState().with(FACING, ctx.getSide());
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return QuarryPlus.ModObjects.MARKER_TYPE.instantiate(pos, state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            if (world.getBlockEntity(pos) instanceof TileMarker marker) {
                marker.tryConnect();
                marker.getArea().ifPresent(area ->
                    player.sendMessage(new LiteralText("%sMarker Area%s: %s".formatted(Formatting.AQUA, Formatting.RESET, area)), false));
            }
        }
        return ActionResult.SUCCESS;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction) {
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(FACING)) {
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
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.empty();
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        Direction direction = state.get(FACING);
        BlockPos blockPos = pos.offset(direction.getOpposite());
        BlockState blockState = world.getBlockState(blockPos);
        return blockState.isSideSolidFullSquare(world, blockPos, direction);
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState neighborState, WorldAccess world, BlockPos pos, BlockPos neighborPos) {
        return state.canPlaceAt(world, pos) ? state : Blocks.AIR.getDefaultState();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        super.neighborUpdate(state, world, pos, block, fromPos, notify);
        if (!world.isClient) {
            if (world.getBlockEntity(pos) instanceof TileMarker marker) {
                marker.rsReceiving = world.isReceivingRedstonePower(pos);
                marker.sync();
            }
        }
    }
}
