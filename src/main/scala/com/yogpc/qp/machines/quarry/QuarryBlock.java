package com.yogpc.qp.machines.quarry;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.integration.ftbchunks.FTBChunksProtectionCheck;
import com.yogpc.qp.integration.wrench.WrenchItems;
import com.yogpc.qp.machines.*;
import com.yogpc.qp.machines.module.ContainerQuarryModule;
import com.yogpc.qp.machines.module.EnergyModuleItem;
import com.yogpc.qp.machines.module.ModuleLootFunction;
import com.yogpc.qp.utils.CombinedBlockEntityTicker;
import com.yogpc.qp.utils.MapMulti;
import com.yogpc.qp.utils.QuarryChunkLoadUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.stream.Stream;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;

public class QuarryBlock extends QPBlock implements EntityBlock {
    public static final String NAME = "quarry";

    public QuarryBlock() {
        super(Properties.of()
            .mapColor(MapColor.METAL)
            .pushReaction(PushReaction.BLOCK)
            .strength(1.5f, 10f)
            .sound(SoundType.STONE), NAME, QuarryItem::new);
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
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction facing = ctx.getPlayer() == null ? Direction.NORTH : ctx.getPlayer().getDirection().getOpposite();
        return defaultBlockState().setValue(FACING, facing);
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        super.setPlacedBy(level, pos, state, entity, stack);
        if (!level.isClientSide && !this.disallowedDim().contains(level.dimension().location())) {
            Direction facing = entity == null ? Direction.NORTH : entity.getDirection().getOpposite();
            if (level.getBlockEntity(pos) instanceof TileQuarry quarry) {
                quarry.setEnchantments(EnchantmentHelper.getEnchantments(stack));
                quarry.setTileDataFromItem(BlockItem.getBlockEntityData(stack));
                var area = findArea(level, pos, facing.getOpposite(), quarry.storage::addItem);
                if (FTBChunksProtectionCheck.isAreaProtected(area, level.dimension())) {
                    if (entity instanceof Player player)
                        player.displayClientMessage(Component.translatable("quarryplus.chat.warn_protected_area"), false);
                } else if (area.maxX() - area.minX() > 1 && area.maxZ() - area.minZ() > 1) {
                    quarry.setState(QuarryState.WAITING, state.setValue(BlockStateProperties.FACING, facing));
                    quarry.setArea(area);
                } else {
                    if (entity instanceof Player player)
                        player.displayClientMessage(Component.translatable("quarryplus.chat.warn_area"), false);
                }
                quarry.updateModules();
                var preForced = QuarryChunkLoadUtil.makeChunkLoaded(level, pos, quarry.enabled);
                quarry.setChunkPreLoaded(preForced);
                quarry.sync();
            }
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock()) && QuarryPlus.config.common.removeFrameAfterQuarryIsRemoved.get()) {
            for (Direction8 dir : Direction8.DIRECTIONS) {
                var offset = pos.offset(dir.vec());
                var maybeFrame = level.getBlockState(offset);
                if (maybeFrame.is(Holder.BLOCK_FRAME) && !maybeFrame.getValue(FrameBlock.DAMMING)) {
                    level.removeBlock(offset, false);
                }
            }
        }
        super.onRemove(state, level, pos, newState, moved);
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
                if (world.getBlockEntity(pos) instanceof TileQuarry quarry) {
                    quarry.setState(QuarryState.WAITING, state);
                    quarry.target = null;
                }
            }
            return InteractionResult.SUCCESS;
        }
        if (!player.isShiftKeyDown()) {
            if (!world.isClientSide) {
                if (world.getBlockEntity(pos) instanceof TileQuarry quarry) {
                    ContainerQuarryModule.InteractionObject.openScreen(quarry, (ServerPlayer) player, getName());
                }
            }
            return InteractionResult.SUCCESS;
        }
        return super.use(state, world, pos, player, hand, hit);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return Holder.QUARRY_TYPE.create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : checkType(blockEntityType, Holder.QUARRY_TYPE,
            CombinedBlockEntityTicker.of(
                this, level,
                PowerTile.getGenerator(),
                EnergyModuleItem.energyModuleTicker(),
                TileQuarry::tick,
                PowerTile.logTicker(),
                MachineStorage.passItems(),
                MachineStorage.passFluid())
        );
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
        var stack = super.getCloneItemStack(state, target, world, pos, player);
        if (world.getBlockEntity(pos) instanceof TileQuarry quarry) {
            QuarryLootFunction.process(stack, quarry);
            EnchantedLootFunction.process(stack, quarry);
            ModuleLootFunction.process(stack, quarry);
        }
        return stack;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        super.neighborChanged(state, world, pos, block, fromPos, notify);
        if (!world.isClientSide) {
            world.getBlockEntity(pos, Holder.QUARRY_TYPE).ifPresent(TileQuarry::updateModules);
        }
    }

    static Area findArea(Level world, BlockPos pos, Direction quarryBehind, Consumer<ItemStack> itemCollector) {
        return Stream.of(quarryBehind, quarryBehind.getCounterClockWise(), quarryBehind.getClockWise())
            .map(pos::relative)
            .map(world::getBlockEntity)
            .mapMulti(MapMulti.cast(QuarryMarker.class))
            .flatMap(m -> m.getArea().stream().peek(a -> m.removeAndGetItems().forEach(itemCollector)))
            .findFirst()
            .map(a -> a.assureY(4))
            .orElse(new Area(pos.relative(quarryBehind).relative(quarryBehind.getCounterClockWise(), 5),
                pos.relative(quarryBehind, 11).relative(quarryBehind.getClockWise(), 5).relative(Direction.UP, 4), quarryBehind));
    }
}
