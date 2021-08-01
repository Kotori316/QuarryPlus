package com.yogpc.qp.machines.marker;

import com.yogpc.qp.QuarryPlus;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.WallTorchBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
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
import org.jetbrains.annotations.Nullable;

public abstract class BlockExMarker extends Block implements BlockEntityProvider {
    private static final VoxelShape STANDING_Shape = VoxelShapes.cuboid(.35, 0, .35, .65, .65, .65);

    public final BlockItem blockItem = new BlockItem(this, new Item.Settings().group(QuarryPlus.CREATIVE_TAB));

    public BlockExMarker() {
        super(Settings.of(Material.DECORATION).luminance(value -> 7).noCollision());
    }

    protected abstract void openGUI(World worldIn, BlockPos pos, PlayerEntity playerIn);

    @Override
    @SuppressWarnings("deprecation")
    public ActionResult onUse(BlockState state, World world, BlockPos pos,
                              PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!player.isSneaking()) {
            if (!world.isClient) {
                openGUI(world, pos, player);
                return ActionResult.CONSUME;
            } else {
                return ActionResult.SUCCESS;
            }
        }
        return super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction) {
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return VoxelShapes.empty();
    }

    @Override
    @SuppressWarnings("deprecation")
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return STANDING_Shape;
    }

    /**
     * Just copied from {@link WallTorchBlock}.
     */
    @Override
    @SuppressWarnings("deprecation")
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        Direction direction = Direction.UP;
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
    public abstract void onPlaced(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack);

    @Override
    public abstract BlockEntity createBlockEntity(BlockPos pos, BlockState state);

    public static class BlockFlexMarker extends BlockExMarker {
        public static final String NAME = "flex_marker";

        @Override
        public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
            return QuarryPlus.ModObjects.FLEX_MARKER_TYPE.instantiate(pos, state);
        }

        @Override
        protected void openGUI(World worldIn, BlockPos pos, PlayerEntity playerIn) {
            playerIn.openHandledScreen(new InteractionObject(pos, QuarryPlus.ModObjects.FLEX_MARKER_HANDLER_TYPE, getTranslationKey()));
        }

        @Override
        public void onPlaced(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
            float rotationYawHead = placer != null ? placer.getHeadYaw() : 0f;
            worldIn.getBlockEntity(pos, QuarryPlus.ModObjects.FLEX_MARKER_TYPE)
                .ifPresent(t -> t.init(Direction.fromRotation(rotationYawHead)));
        }

    }

    public static class Block16Marker extends BlockExMarker {
        private static final Range RANGE = new Range(0, 360);
        public static final String NAME = "marker16";

        @Override
        protected void openGUI(World worldIn, BlockPos pos, PlayerEntity playerIn) {
            playerIn.openHandledScreen(new InteractionObject(pos, QuarryPlus.ModObjects.MARKER_16_HANDLER_TYPE, getTranslationKey()));
        }

        @Override
        public void onPlaced(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
            float angle = RANGE.convert(placer != null ? placer.getHeadYaw() : 0f);
            Direction.AxisDirection z = angle < 90 || angle >= 270 ? Direction.AxisDirection.POSITIVE : Direction.AxisDirection.NEGATIVE;
            Direction.AxisDirection x = angle > 180 ? Direction.AxisDirection.POSITIVE : Direction.AxisDirection.NEGATIVE;
            worldIn.getBlockEntity(pos, QuarryPlus.ModObjects.MARKER_16_TYPE).ifPresent(t -> t.init(x, z));
        }

        @Override
        public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
            return QuarryPlus.ModObjects.MARKER_16_TYPE.instantiate(pos, state);
        }

    }

    public static final String GUI_FLEX_ID = QuarryPlus.modID + ":gui_" + "flex_marker";
    public static final String GUI_16_ID = QuarryPlus.modID + ":gui_" + "marker16";

    private record InteractionObject(BlockPos pos, ScreenHandlerType<?> type,
                                     String name) implements ExtendedScreenHandlerFactory {

        @Override
        public Text getDisplayName() {
            return new TranslatableText(name);
        }

        @Override
        public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
            return new ContainerMarker(syncId, player, this.pos, type);
        }

        @Override
        public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
            buf.writeBlockPos(pos);
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
