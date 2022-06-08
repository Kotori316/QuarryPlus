package com.yogpc.qp.machines.advpump;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.integration.wrench.WrenchItems;
import com.yogpc.qp.machines.EnchantedLootFunction;
import com.yogpc.qp.machines.MachineStorage;
import com.yogpc.qp.machines.PowerTile;
import com.yogpc.qp.machines.QPBlock;
import com.yogpc.qp.machines.module.ContainerQuarryModule;
import com.yogpc.qp.utils.CombinedBlockEntityTicker;
import com.yogpc.qp.utils.QuarryChunkLoadUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

public class BlockAdvPump extends QPBlock implements EntityBlock {
    public static final String NAME = "adv_pump";

    public BlockAdvPump() {
        super(QPBlock.Properties.of(Material.METAL)
            .strength(1.5f, 10f)
            .sound(SoundType.STONE), NAME, ItemAdvPump::new);
        registerDefaultState(getStateDefinition().any()
            .setValue(WORKING, false));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return Holder.ADV_PUMP_TYPE.create(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(WORKING);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return world.isClientSide ? null : checkType(type, Holder.ADV_PUMP_TYPE,
            new CombinedBlockEntityTicker<>(PowerTile.getGenerator(), TileAdvPump::tick, PowerTile.logTicker(), MachineStorage.passFluid()));
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
                if (world.getBlockEntity(pos) instanceof TileAdvPump pump) {
                    pump.reset();
                    pump.deleteFluid = !pump.deleteFluid;
                    player.displayClientMessage(Component.literal("AdvPump DeleteFluid: " + pump.deleteFluid), false);
                }
            }
            return InteractionResult.SUCCESS;
        }
        if (!player.isShiftKeyDown()) {
            if (!world.isClientSide) {
                if (world.getBlockEntity(pos) instanceof TileAdvPump pump) {
                    ContainerQuarryModule.InteractionObject.openGUI(pump, (ServerPlayer) player, getName());
                }
            }
            return InteractionResult.SUCCESS;
        }
        return super.use(state, world, pos, player, hand, hit);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        super.setPlacedBy(level, pos, state, entity, stack);
        if (!level.isClientSide) {
            if (level.getBlockEntity(pos) instanceof TileAdvPump pump) {
                var preForced = QuarryChunkLoadUtil.makeChunkLoaded(level, pos, pump.enabled);
                pump.setEnchantment(EnchantmentEfficiency.fromMap(EnchantmentHelper.getEnchantments(stack)));
                pump.setChunkPreLoaded(preForced);
                pump.updateModule();
            }
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            if (level.getBlockEntity(pos) instanceof TileAdvPump pump) {
                Containers.dropContents(level, pos, pump.getModuleInventory());
            }
            super.onRemove(state, level, pos, newState, moved);
        }
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
        // Called in client.
        ItemStack stack = super.getCloneItemStack(state, target, world, pos, player);
        if (world.getBlockEntity(pos) instanceof TileAdvPump pump) {
            EnchantedLootFunction.process(stack, pump);
        }
        return stack;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        super.neighborChanged(state, world, pos, block, fromPos, notify);
        if (!world.isClientSide) {
            world.getBlockEntity(pos, Holder.ADV_PUMP_TYPE).ifPresent(TileAdvPump::updateModule);
        }
    }
}
