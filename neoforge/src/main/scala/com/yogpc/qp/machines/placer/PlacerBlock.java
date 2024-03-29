package com.yogpc.qp.machines.placer;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.QPBlock;
import com.yogpc.qp.utils.CombinedBlockEntityTicker;
import com.yogpc.qp.utils.ScreenUtil;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.TRIGGERED;

public class PlacerBlock extends QPBlock implements EntityBlock {
    public static final String NAME = "placer_plus";

    public PlacerBlock() {
        super(Properties.of()
            .mapColor(MapColor.METAL)
            .pushReaction(PushReaction.BLOCK).strength(1.2f), NAME);
        registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH).setValue(TRIGGERED, Boolean.FALSE));
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!QuarryPlus.config.enableMap.enabled(NAME)) {
            if (!world.isClientSide)
                player.displayClientMessage(Component.translatable("quarryplus.chat.disable_message", getName()), true);
            return InteractionResult.sidedSuccess(world.isClientSide);
        }
        if (!player.isCrouching()) {
            if (!world.isClientSide) {
                ItemStack stack = player.getItemInHand(hand);
                if (stack.getItem() == Items.REDSTONE_TORCH) {
                    world.getBlockEntity(pos, Holder.PLACER_TYPE).ifPresent(t -> {
                        t.cycleRedstoneMode();
                        player.displayClientMessage(Component.translatable("quarryplus.chat.placer_rs", t.redstoneMode), false);
                    });
                } else {
                    world.getBlockEntity(pos, Holder.PLACER_TYPE).ifPresent(o ->
                        ScreenUtil.openScreen((ServerPlayer) player, o, pos));
                }
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            if (level.getBlockEntity(pos) instanceof PlacerTile placer)
                Containers.dropContents(level, pos, placer);
            super.onRemove(state, level, pos, newState, moved);
        }
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return Holder.PLACER_TYPE.create(pos, state);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(FACING, ctx.getNearestLookingDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING, TRIGGERED);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("quarryplus.tooltip.placer_plus"));
        }
    }

    // Placer Works

    @Override
    @SuppressWarnings("deprecation")
    public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource rand) {
        super.tick(state, worldIn, pos, rand);
        boolean isPowered = state.getValue(TRIGGERED);
        worldIn.getBlockEntity(pos, Holder.PLACER_TYPE).ifPresent(tile -> {
            if (isPowered) {
                tile.placeBlock();
            } else {
                tile.breakBlock();
            }
        });
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : checkType(type, Holder.PLACER_TYPE, CombinedBlockEntityTicker.of(
            this, level,
            (l, p, s, placer) -> placer.tick()
        ));
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
        // Copied from net.minecraft.block.DispenserBlock#neighborChanged
        Direction facing = state.getValue(FACING);
        boolean poweredNow = isPoweredToWork(worldIn, pos, facing);
        boolean poweredOld = state.getValue(TRIGGERED);
        if (poweredNow && !poweredOld) {
            if (worldIn.getBlockEntity(pos, Holder.PLACER_TYPE)
                .filter(p -> p.redstoneMode.isPulse()).isPresent()) {
                worldIn.scheduleTick(pos, this, 1);
            }
            worldIn.setBlock(pos, state.setValue(TRIGGERED, Boolean.TRUE), Block.UPDATE_INVISIBLE);
        } else if (!poweredNow && poweredOld) {
            if (worldIn.getBlockEntity(pos, Holder.PLACER_TYPE)
                .filter(p -> p.redstoneMode.isPulse()).isPresent()) {
                worldIn.scheduleTick(pos, this, 1);
            }
            worldIn.setBlock(pos, state.setValue(TRIGGERED, Boolean.FALSE), Block.UPDATE_INVISIBLE);
        }
    }

    private static final Direction[] DIRECTIONS = Direction.values().clone();

    public static boolean isPoweredToWork(Level worldIn, BlockPos pos, @Nullable Direction currentFacing) {
        return Arrays.stream(DIRECTIONS).filter(Predicate.isEqual(currentFacing).negate())
            .anyMatch(f -> worldIn.hasSignal(pos.relative(f), f)) || worldIn.hasNeighborSignal(pos.above());
    }

}
