package com.yogpc.qp.machines.module;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.QPBlock;
import javax.annotation.Nonnull;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;

public class ExpPumpBlock extends QPBlock implements EntityBlock, QuarryModuleProvider.Block {
    public static final String NAME = "exp_pump";

    public ExpPumpBlock() {
        super(Properties.of(Material.METAL).strength(3.0f), NAME);
        registerDefaultState(getStateDefinition().any()
            .setValue(WORKING, false));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return Holder.EXP_PUMP_TYPE.create(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(WORKING);
    }

    @Override
    public QuarryModule getModule(@Nonnull Level level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof ExpPumpTile expPump) {
            return expPump.getModule();
        } else {
            return QuarryModule.Constant.DUMMY;
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!QuarryPlus.config.enableMap.enabled(NAME)) {
            if (!level.isClientSide)
                player.displayClientMessage(new TranslatableComponent("quarryplus.chat.disable_message", getName()), false);
            return InteractionResult.SUCCESS;
        }
        if (!level.isClientSide) {
            if (level.getBlockEntity(pos) instanceof ExpPumpTile expPump && expPump.getModule().getExp() > 0) {
                player.giveExperiencePoints(expPump.getModule().getExp());
                player.displayClientMessage(new TranslatableComponent("quarryplus.chat.give_exp", expPump.getModule().getExp()), false);
                expPump.getModule().setExp(0, false);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            if (level.getBlockEntity(pos) instanceof ExpPumpTile expPump && expPump.getModule().getExp() > 0) {
                state.getBlock().popExperience((ServerLevel) level, pos, expPump.getModule().getExp());
            }
            super.onRemove(state, level, pos, newState, moved);
        }
    }
}
