package com.yogpc.qp.machines.filler;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.PowerTile;
import com.yogpc.qp.machines.QPBlock;
import com.yogpc.qp.utils.CombinedBlockEntityTicker;
import com.yogpc.qp.utils.QuarryChunkLoadUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public final class FillerBlock extends QPBlock implements EntityBlock {
    public static final String NAME = "filler";

    public FillerBlock() {
        super(Properties.of(Material.METAL)
            .strength(1.5f, 10f)
            .sound(SoundType.STONE), NAME);
    }

    @Override
    public FillerEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return QuarryPlus.ModObjects.FILLER_TYPE.create(pPos, pState);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, blockIn, fromPos, isMoving);
        if (!level.isClientSide && level.hasNeighborSignal(pos)) {
            level.getBlockEntity(pos, QuarryPlus.ModObjects.FILLER_TYPE)
                .ifPresent(f -> f.start(FillerEntity.Action.BOX));
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        if (!player.isShiftKeyDown()) {
            if (!level.isClientSide && level.getBlockEntity(pos) instanceof FillerEntity filler) {
                player.openMenu(filler);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        super.setPlacedBy(level, pos, state, entity, stack);
        if (!level.isClientSide) {
            level.getBlockEntity(pos, QuarryPlus.ModObjects.FILLER_TYPE)
                .ifPresent(t -> {
                    var preForced = QuarryChunkLoadUtil.makeChunkLoaded(level, pos);
                    t.setChunkPreLoaded(preForced);
                });
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof FillerEntity filler) {
                Containers.dropContents(level, pos, filler.container);
                level.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, level, pos, newState, moved);
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> entityType) {
        return pLevel.isClientSide ? null : createTickerHelper(entityType, QuarryPlus.ModObjects.FILLER_TYPE, new CombinedBlockEntityTicker<>(
            PowerTile.getGenerator(),
            (l, p, s, t) -> t.tick(),
            PowerTile.logTicker()
        ));
    }
}
