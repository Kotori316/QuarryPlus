package com.yogpc.qp.machines.bookmover;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.PowerTile;
import com.yogpc.qp.machines.QPBlock;
import com.yogpc.qp.utils.CombinedBlockEntityTicker;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

public class BookMoverBlock extends QPBlock implements EntityBlock {
    public static final String NAME = "book_mover";
    public static final String GUI_ID = QuarryPlus.modID + ":gui_" + NAME;

    public BookMoverBlock() {
        super(Properties.of()
                .mapColor(MapColor.METAL)
                .pushReaction(PushReaction.BLOCK).strength(1.2f), NAME);
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!QuarryPlus.config.enableMap.enabled(NAME)) {
            if (!world.isClientSide)
                player.displayClientMessage(Component.translatable("quarryplus.chat.disable_message", getName()), true);
            return InteractionResult.sidedSuccess(world.isClientSide);
        }
        if (!player.isShiftKeyDown()) {
            if (!world.isClientSide && world.getBlockEntity(pos) instanceof BookMoverEntity mover) {
                NetworkHooks.openScreen((ServerPlayer) player, mover, pos);
            }
            return InteractionResult.sidedSuccess(world.isClientSide);
        }
        return super.use(state, world, pos, player, hand, hit);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            if (level.getBlockEntity(pos) instanceof BookMoverEntity mover) {
                Containers.dropContents(level, pos, mover);
            }
            super.onRemove(state, level, pos, newState, moved);
        }
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return Holder.BOOK_MOVER_TYPE.create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : checkType(type, Holder.BOOK_MOVER_TYPE,
                new CombinedBlockEntityTicker<>(PowerTile.getGenerator(), (l, p, s, t) -> t.workInTick(), PowerTile.logTicker()));
    }
}
