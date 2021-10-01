package com.yogpc.qp.machines.mini_quarry;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.EnchantmentLevel;
import com.yogpc.qp.machines.QPBlock;
import com.yogpc.qp.utils.QuarryChunkLoadUtil;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fmllegacy.network.NetworkHooks;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;

public class MiniQuarryBlock extends QPBlock implements EntityBlock {
    public static final String NAME = "mini_quarry";

    public MiniQuarryBlock() {
        super(Properties.of(Material.METAL).strength(1.5f, 10f).sound(SoundType.STONE),
            NAME, MiniQuarryItem::new);

        registerDefaultState(getStateDefinition().any()
            .setValue(WORKING, false)
            .setValue(BlockStateProperties.FACING, Direction.NORTH));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return Holder.MINI_QUARRY_TYPE.create(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(WORKING, BlockStateProperties.FACING);
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!QuarryPlus.config.enableMap.enabled(NAME)) {
            if (!level.isClientSide)
                player.displayClientMessage(new TranslatableComponent("quarryplus.chat.disable_message", getName()), false);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                level.getBlockEntity(pos, Holder.MINI_QUARRY_TYPE)
                    .ifPresent(t -> NetworkHooks.openGui((ServerPlayer) player, t, pos));
            }
            return InteractionResult.SUCCESS;
        } else {
            return InteractionResult.PASS;
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction facing = ctx.getPlayer() == null ? Direction.NORTH : ctx.getPlayer().getDirection().getOpposite();
        return defaultBlockState().setValue(FACING, facing);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        super.setPlacedBy(level, pos, state, entity, stack);
        if (!level.isClientSide) {
            level.getBlockEntity(pos, Holder.MINI_QUARRY_TYPE)
                .ifPresent(t -> {
                    t.setEnchantments(EnchantmentLevel.fromItem(stack));
                    var preForced = QuarryChunkLoadUtil.makeChunkLoaded(level, pos, t.enabled);
                    t.setChunkPreLoaded(preForced);
                });
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, blockIn, fromPos, isMoving);
        if (!level.isClientSide) {
            boolean powered = level.hasNeighborSignal(pos);
            level.getBlockEntity(pos, Holder.MINI_QUARRY_TYPE)
                .ifPresent(t -> {
                    if (powered) {
                        if (!t.rs) {
                            t.gotRSPulse();
                        }
                    }
                    t.rs = powered;
                });
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof MiniQuarryTile tile) {
                Containers.dropContents(level, pos, tile.getInv());
                level.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, level, pos, newState, moved);
        }
    }
}
