package com.yogpc.qp.machines.marker;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.QPBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
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
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.WallTorchBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public abstract class BlockExMarker extends QPBlock implements EntityBlock {
    private static final VoxelShape STANDING_Shape = Shapes.box(.35, 0, .35, .65, .65, .65);

    public BlockExMarker(String name) {
        super(Properties.of(Material.DECORATION).lightLevel(value -> 7).noCollission(), name);
    }

    protected abstract void openGUI(Level worldIn, BlockPos pos, Player playerIn);

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level world, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        if (!player.isShiftKeyDown()) {
            if (!world.isClientSide) {
                openGUI(world, pos, player);
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

    public static class BlockFlexMarker extends BlockExMarker {
        public static final String NAME = "flex_marker";

        public BlockFlexMarker() {
            super(NAME);
        }

        @Override
        public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
            return Holder.FLEX_MARKER_TYPE.create(pos, state);
        }

        @Override
        protected void openGUI(Level worldIn, BlockPos pos, Player playerIn) {
            NetworkHooks.openGui(((ServerPlayer) playerIn), new InteractionObject(pos, Holder.FLEX_MARKER_MENU_TYPE, getDescriptionId()), pos);
        }

        @Override
        public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
            float rotationYawHead = placer != null ? placer.getYHeadRot() : 0f;
            level.getBlockEntity(pos, Holder.FLEX_MARKER_TYPE)
                .ifPresent(t -> t.init(Direction.fromYRot(rotationYawHead)));
        }

    }

    public static class Block16Marker extends BlockExMarker {
        private static final Range RANGE = new Range(0, 360);
        public static final String NAME = "marker16";

        public Block16Marker() {
            super(NAME);
        }

        @Override
        protected void openGUI(Level worldIn, BlockPos pos, Player playerIn) {
            NetworkHooks.openGui(((ServerPlayer) playerIn), new InteractionObject(pos, Holder.MARKER_16_MENU_TYPE, getDescriptionId()), pos);
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

    public static final String GUI_FLEX_ID = QuarryPlus.modID + ":gui_" + "flex_marker";
    public static final String GUI_16_ID = QuarryPlus.modID + ":gui_" + "marker16";

    private record InteractionObject(BlockPos pos, MenuType<?> type, String name) implements MenuProvider {

        @Override
        public Component getDisplayName() {
            return new TranslatableComponent(name);
        }

        @Override
        public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
            return new ContainerMarker(syncId, player, this.pos, type);
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
