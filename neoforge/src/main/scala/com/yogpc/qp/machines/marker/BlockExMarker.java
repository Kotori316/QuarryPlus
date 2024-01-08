package com.yogpc.qp.machines.marker;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.QPBlock;
import com.yogpc.qp.utils.ScreenUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;

public abstract class BlockExMarker extends QPBlock implements EntityBlock {
    private static final VoxelShape STANDING_Shape = Shapes.box(.35, 0, .35, .65, .65, .65);

    protected BlockExMarker(Properties properties, String name) {
        super(properties, name);
    }

    protected abstract void openScreen(Level worldIn, BlockPos pos, Player playerIn);

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level world, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        if (!player.isShiftKeyDown()) {
            if (!world.isClientSide) {
                openScreen(world, pos, player);
                return InteractionResult.CONSUME;
            } else {
                return InteractionResult.SUCCESS;
            }
        }
        return super.use(state, world, pos, player, hand, hit);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean skipRendering(BlockState state, BlockState stateFrom, Direction direction) {
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return STANDING_Shape;
    }

    /**
     * Just copied from {@link WallTorchBlock}.
     */
    @Override
    @SuppressWarnings("deprecation")
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        Direction direction = Direction.UP;
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
    public abstract void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack);

    @Override
    public abstract BlockEntity newBlockEntity(BlockPos pos, BlockState state);

    protected static abstract class WaterloggedMarker extends BlockExMarker implements SimpleWaterloggedBlock {
        protected WaterloggedMarker(Properties properties, String name) {
            super(properties, name);
            this.registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false));
        }

        @Override
        protected ResourceLocation getConfigName() {
            return getBaseBlock().getRegistryName();
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            super.createBlockStateDefinition(builder);
            builder.add(WATERLOGGED);
        }

        @Override
        public BlockState getStateForPlacement(BlockPlaceContext ctx) {
            var fluid = ctx.getLevel().getFluidState(ctx.getClickedPos());
            return defaultBlockState().setValue(WATERLOGGED, fluid.is(Fluids.WATER));
        }

        protected abstract BlockExMarker getBaseBlock();

        @Override
        public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
            return BlockWaterloggedMarker.SHAPE;
        }

        @Override
        protected void openScreen(Level worldIn, BlockPos pos, Player playerIn) {
            getBaseBlock().openScreen(worldIn, pos, playerIn);
        }

        @Override
        public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
            return true;
        }

        @Override
        public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
            getBaseBlock().setPlacedBy(level, pos, state, placer, stack);
        }

        @Override
        public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
            return getBaseBlock().newBlockEntity(pos, state);
        }

        @Override
        public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pCurrentPos, BlockPos pNeighborPos) {
            if (state.getValue(WATERLOGGED)) {
                level.scheduleTick(pCurrentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
            }
            return super.updateShape(state, direction, neighborState, level, pCurrentPos, pNeighborPos);
        }

        @Override
        @SuppressWarnings("deprecation")
        public FluidState getFluidState(BlockState state) {
            return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
        }
    }

    public static class BlockFlexMarker extends BlockExMarker {
        public static final String NAME = "flex_marker";

        public BlockFlexMarker() {
            super(Properties.of().mapColor(MapColor.NONE).pushReaction(PushReaction.DESTROY).instabreak().lightLevel(value -> 7).noCollission(), NAME);
        }

        @Override
        public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
            return Holder.FLEX_MARKER_TYPE.create(pos, state);
        }

        @Override
        protected void openScreen(Level worldIn, BlockPos pos, Player playerIn) {
            ScreenUtil.openScreen((ServerPlayer) playerIn, new InteractionObject(pos, Holder.FLEX_MARKER_MENU_TYPE, getDescriptionId(), 29, 139), pos);
        }

        @Override
        public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
            float rotationYawHead = placer != null ? placer.getYHeadRot() : 0f;
            level.getBlockEntity(pos, Holder.FLEX_MARKER_TYPE)
                .ifPresent(t -> t.init(Direction.fromYRot(rotationYawHead)));
        }

    }

    public static class BlockWaterloggedFlexMarker extends WaterloggedMarker {
        public static final String NAME = "waterlogged_flex_marker";

        public BlockWaterloggedFlexMarker() {
            super(Properties.of().mapColor(MapColor.NONE).pushReaction(PushReaction.DESTROY).instabreak(), NAME);
        }

        @Override
        protected BlockExMarker getBaseBlock() {
            return Holder.BLOCK_FLEX_MARKER;
        }
    }

    public static class Block16Marker extends BlockExMarker {
        private static final Range RANGE = new Range(0, 360);
        public static final String NAME = "marker16";

        public Block16Marker() {
            super(Properties.of().mapColor(MapColor.NONE).pushReaction(PushReaction.DESTROY).instabreak().lightLevel(value -> 7).noCollission(), NAME);
        }

        @Override
        protected void openScreen(Level worldIn, BlockPos pos, Player playerIn) {
            ScreenUtil.openScreen((ServerPlayer) playerIn, new InteractionObject(pos, Holder.MARKER_16_MENU_TYPE, getDescriptionId(), 29, 107), pos);
        }

        @Override
        public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
            float angle = RANGE.convert(placer != null ? placer.getYHeadRot() : 0f);
            Direction.AxisDirection z = angle < 90 || angle >= 270 ? Direction.AxisDirection.POSITIVE : Direction.AxisDirection.NEGATIVE;
            Direction.AxisDirection x = angle > 180 ? Direction.AxisDirection.POSITIVE : Direction.AxisDirection.NEGATIVE;
            level.getBlockEntity(pos, Holder.MARKER_16_TYPE).ifPresent(t -> t.init(x, z));
        }

        @Override
        public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
            return Holder.MARKER_16_TYPE.create(pos, state);
        }

    }

    public static class BlockWaterlogged16Marker extends WaterloggedMarker {
        public static final String NAME = "waterlogged_marker16";

        public BlockWaterlogged16Marker() {
            super(Properties.of().mapColor(MapColor.NONE).pushReaction(PushReaction.DESTROY).instabreak(), NAME);
        }

        @Override
        protected BlockExMarker getBaseBlock() {
            return Holder.BLOCK_16_MARKER;
        }
    }

    public static final String GUI_FLEX_ID = QuarryPlus.modID + ":gui_" + "flex_marker";
    public static final String GUI_16_ID = QuarryPlus.modID + ":gui_" + "marker16";

    private record InteractionObject(BlockPos pos, MenuType<?> type, String name,
                                     int inventoryX, int inventoryY) implements MenuProvider {

        @Override
        public Component getDisplayName() {
            return Component.translatable(name);
        }

        @Override
        public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
            return new ContainerMarker(syncId, player, this.pos, type, inventoryX, inventoryY);
        }
    }

    public static class Range {
        private final float min;
        private final float max;
        private final float distance;

        public Range(float min, float max) {
            this.min = min;
            this.max = max;
            if (max < min) {
                throw new IllegalArgumentException(String.format("min is grater than max. Min: %f, Max:%f", min, max));
            }
            this.distance = max - min;
        }

        public float convert(float f) {
            if (f < min) {
                int i = (int) ((min - f) / distance) + 1;
                return convert(f + distance * i);
            } else if (f >= max) {
                int i = (int) ((f - max) / distance) + 1;
                return convert(f - distance * i);
            } else {
                return f;
            }
        }
    }

}
