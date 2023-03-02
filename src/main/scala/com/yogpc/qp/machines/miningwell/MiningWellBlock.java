package com.yogpc.qp.machines.miningwell;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.integration.wrench.WrenchItems;
import com.yogpc.qp.machines.EnchantedLootFunction;
import com.yogpc.qp.machines.MachineStorage;
import com.yogpc.qp.machines.PowerTile;
import com.yogpc.qp.machines.QPBlock;
import com.yogpc.qp.utils.CombinedBlockEntityTicker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;

public class MiningWellBlock extends QPBlock implements EntityBlock {
    public static final String NAME = "mining_well";

    public MiningWellBlock() {
        super(Properties.of(Material.METAL).strength(1.5f), NAME, MiningWellItem::new);
        registerDefaultState(getStateDefinition().any()
            .setValue(WORKING, false)
            .setValue(BlockStateProperties.FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(WORKING, BlockStateProperties.FACING);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return Holder.MINING_WELL_TYPE.create(pos, state);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction facing = ctx.getPlayer() == null ? Direction.NORTH : ctx.getPlayer().getDirection().getOpposite();
        return defaultBlockState().setValue(FACING, facing);
    }

    /**
     * Called when player placed this block.
     * In both client and server side.
     * Required to set config related to player or item.
     */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        super.setPlacedBy(level, pos, state, entity, stack);
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof MiningWellTile miningWellTile) {
            int efficiencyLevel = stack.getEnchantmentLevel(Enchantments.BLOCK_EFFICIENCY);
            miningWellTile.setEfficiencyLevel(efficiencyLevel);
        }
    }

    /**
     * Called after {@link #setPlacedBy(Level, BlockPos, BlockState, LivingEntity, ItemStack)} is called.
     * Even the block is placed via command, this method is called while {@link #setPlacedBy(Level, BlockPos, BlockState, LivingEntity, ItemStack)}
     * is only for player placement.
     * In server side only.
     * Required to configure setting related to world.
     */
    @Override
    @SuppressWarnings("deprecation")
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (state.getBlock() != oldState.getBlock() && !level.isClientSide && level.getBlockEntity(pos) instanceof MiningWellTile miningWellTile) {
            miningWellTile.digMinY = level.getMinBuildHeight();
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!QuarryPlus.config.enableMap.enabled(NAME)) {
            if (!world.isClientSide)
                player.displayClientMessage(Component.translatable("quarryplus.chat.disable_message", getName()), true);
            return InteractionResult.sidedSuccess(world.isClientSide);
        }
        var stack = player.getItemInHand(hand);
        if (WrenchItems.isWrenchItem(stack)) {
            if (!world.isClientSide) {
                world.getBlockEntity(pos, Holder.MINING_WELL_TYPE)
                    .ifPresent(MiningWellTile::reset);
            }
            return InteractionResult.SUCCESS;
        }
        return super.use(state, world, pos, player, hand, hit);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
        var stack = super.getCloneItemStack(state, target, world, pos, player);
        if (world.getBlockEntity(pos) instanceof MiningWellTile miningWellTile) {
            EnchantedLootFunction.process(stack, miningWellTile);
        }
        return stack;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : checkType(blockEntityType, Holder.MINING_WELL_TYPE,
            new CombinedBlockEntityTicker<>(PowerTile.getGenerator(), (l, p, s, t) -> t.tick(), PowerTile.logTicker(),
                MachineStorage.passItems(), MachineStorage.passFluid()));
    }
}
