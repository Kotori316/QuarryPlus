package com.yogpc.qp.machine.quarry;

import com.yogpc.qp.machine.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public abstract class QuarryBlock extends QpEntityBlock {
    public static final String NAME = "quarry";

    protected QuarryBlock(Function<QpBlock, ? extends BlockItem> itemGenerator) {
        super(Properties.of()
            .mapColor(MapColor.METAL)
            .pushReaction(PushReaction.BLOCK)
            .strength(1.5f, 10f)
            .sound(SoundType.STONE), NAME, itemGenerator);
        registerDefaultState(getStateDefinition().any()
            .setValue(QpBlockProperty.WORKING, false)
            .setValue(BlockStateProperties.FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(QpBlockProperty.WORKING, BlockStateProperties.FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction facing = ctx.getPlayer() == null ? Direction.NORTH : ctx.getPlayer().getDirection().getOpposite();
        return defaultBlockState().setValue(BlockStateProperties.FACING, facing);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) {
            return createTickerHelper(blockEntityType, this.<QuarryEntity>getBlockEntityType().orElse(null), CombinedBlockEntityTicker.of(this, level,
                QuarryEntity::clientTick
            ));
        }
        return createTickerHelper(blockEntityType, this.<QuarryEntity>getBlockEntityType().orElse(null), CombinedBlockEntityTicker.of(this, level,
            PowerEntity.logTicker(),
            QuarryEntity::serverTick
        ));
    }

    // Action
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            var entity = this.<QuarryEntity>getBlockEntityType().map(t -> t.getBlockEntity(level, pos)).orElse(null);
            if (entity != null) {
                return InteractionResult.SUCCESS;
            }
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide) {
            if (level.getBlockEntity(pos) instanceof QuarryEntity quarry) {
                var facing = state.getValue(BlockStateProperties.FACING);
                {
                    // set initial area
                    var base = pos.relative(facing.getOpposite());
                    var corner1 = base.relative(facing.getClockWise(), 5).above(4);
                    var corner2 = base.relative(facing.getCounterClockWise(), 5).relative(facing.getOpposite(), 10);
                    var area = new Area(corner1, corner2, facing.getOpposite());
                    quarry.setArea(area);
                }
                quarry.setState(QuarryState.WAITING, state);
            }
        }
    }
}
