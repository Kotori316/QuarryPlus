package com.yogpc.qp.machines.placer;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.QPBlock;
import com.yogpc.qp.utils.ScreenUtil;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.TRIGGERED;

public final class RemotePlacerBlock extends QPBlock implements EntityBlock {
    public static final String NAME = "remote_placer";

    public RemotePlacerBlock() {
        super(Properties.of()
            .mapColor(MapColor.METAL)
            .pushReaction(PushReaction.BLOCK).strength(1.2f), NAME);
        registerDefaultState(getStateDefinition().any().setValue(TRIGGERED, Boolean.FALSE));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return Holder.REMOTE_PLACER_TYPE.create(pos, state);
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
                    world.getBlockEntity(pos, Holder.REMOTE_PLACER_TYPE).ifPresent(t -> {
                        t.cycleRedstoneMode();
                        player.displayClientMessage(Component.translatable("quarryplus.chat.placer_rs", t.redstoneMode), false);
                    });
                } else {
                    world.getBlockEntity(pos, Holder.REMOTE_PLACER_TYPE).ifPresent(o ->
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
            if (level.getBlockEntity(pos) instanceof RemotePlacerTile placer)
                Containers.dropContents(level, pos, placer);
            super.onRemove(state, level, pos, newState, moved);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(TRIGGERED);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
        // Copied from net.minecraft.block.DispenserBlock#neighborChanged
        boolean poweredNow = PlacerBlock.isPoweredToWork(worldIn, pos, null);
        boolean poweredOld = state.getValue(TRIGGERED);
        if (poweredNow && !poweredOld) {
            if (worldIn.getBlockEntity(pos, Holder.REMOTE_PLACER_TYPE)
                .filter(p -> p.redstoneMode.isPulse()).isPresent()) {
                worldIn.scheduleTick(pos, this, 1);
            }
            worldIn.setBlock(pos, state.setValue(TRIGGERED, Boolean.TRUE), Block.UPDATE_INVISIBLE);
        } else if (!poweredNow && poweredOld) {
            if (worldIn.getBlockEntity(pos, Holder.REMOTE_PLACER_TYPE)
                .filter(p -> p.redstoneMode.isPulse()).isPresent()) {
                worldIn.scheduleTick(pos, this, 1);
            }
            worldIn.setBlock(pos, state.setValue(TRIGGERED, Boolean.FALSE), Block.UPDATE_INVISIBLE);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource rand) {
        super.tick(state, worldIn, pos, rand);
        boolean isPowered = state.getValue(TRIGGERED);
        worldIn.getBlockEntity(pos, Holder.REMOTE_PLACER_TYPE).ifPresent(tile -> {
            if (isPowered) {
                tile.placeBlock();
            } else {
                tile.breakBlock();
            }
        });
    }
}
