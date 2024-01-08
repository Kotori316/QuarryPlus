package com.yogpc.qp.machines.advquarry;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.integration.wrench.WrenchItems;
import com.yogpc.qp.machines.*;
import com.yogpc.qp.machines.module.EnergyModuleItem;
import com.yogpc.qp.machines.module.ModuleLootFunction;
import com.yogpc.qp.machines.module.QuarryModuleProvider;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.utils.CombinedBlockEntityTicker;
import com.yogpc.qp.utils.MapMulti;
import com.yogpc.qp.utils.QuarryChunkLoadUtil;
import com.yogpc.qp.utils.ScreenUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.function.Consumer;
import java.util.stream.Stream;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;

public class BlockAdvQuarry extends QPBlock implements EntityBlock {
    public static final String NAME = "adv_quarry";

    public BlockAdvQuarry() {
        super(Properties.of()
            .mapColor(MapColor.METAL)
            .pushReaction(PushReaction.BLOCK)
            .strength(1.5f, 10f)
            .sound(SoundType.STONE), NAME, ItemAdvQuarry::new);
        registerDefaultState(getStateDefinition().any()
            .setValue(WORKING, false)
            .setValue(FACING, Direction.NORTH));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return Holder.ADV_QUARRY_TYPE.create(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(WORKING, FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction facing = ctx.getPlayer() == null ? Direction.NORTH : ctx.getPlayer().getDirection().getOpposite();
        return defaultBlockState().setValue(FACING, facing);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        super.neighborChanged(state, world, pos, block, fromPos, notify);
        if (!world.isClientSide) {
            world.getBlockEntity(pos, Holder.ADV_QUARRY_TYPE).ifPresent(TileAdvQuarry::updateModule);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                 Player player, InteractionHand hand, BlockHitResult hit) {
        if (!QuarryPlus.config.enableMap.enabled(NAME)) {
            if (!level.isClientSide)
                player.displayClientMessage(Component.translatable("quarryplus.chat.disable_message", getName()), true);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        var stack = player.getItemInHand(hand);
        if (WrenchItems.isWrenchItem(stack)) {
            if (!level.isClientSide && level.getBlockEntity(pos) instanceof TileAdvQuarry quarry) {
                quarry.setAction(AdvQuarryAction.Waiting.WAITING);
                player.displayClientMessage(Component.translatable("quarryplus.chat.quarry.restart"), false);
            }
            return InteractionResult.SUCCESS;
        }
        if (!player.isShiftKeyDown()) {
            if (!level.isClientSide && level.getBlockEntity(pos) instanceof TileAdvQuarry quarry) {
                if (stack.getItem() instanceof QuarryModuleProvider.Item) {
                    quarry.openModuleGui((ServerPlayer) player);
                } else {
                    ScreenUtil.openScreen((ServerPlayer) player, quarry, pos);
                }
            }
            return InteractionResult.SUCCESS;
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (state.getBlock() != oldState.getBlock() &&
            !level.isClientSide &&
            !this.disallowedDim().contains(level.dimension().location()) &&
            level.getBlockEntity(pos) instanceof TileAdvQuarry quarry) {
            quarry.initialSetting();
            Direction facing = state.getValue(FACING);
            if (!quarry.setArea(findArea(level, pos, facing.getOpposite(), quarry.getStorage()::addItem))) {
                // Area is not set because marker area is invalid. Use default.
                var defaultArea = createDefaultArea(pos, facing.getOpposite(), QuarryPlus.config.common.chunkDestroyerLimit.get());
                if (!quarry.setArea(defaultArea)) {
                    // Unreachable
                    AdvQuarry.LOGGER.warn(AdvQuarry.BLOCK, "The default area is invalid. Area={}, Limit={}, Pos={}",
                        defaultArea, QuarryPlus.config.common.chunkDestroyerLimit.get(), pos);
                }
            }
            var preForced = QuarryChunkLoadUtil.makeChunkLoaded(level, pos, quarry.enabled);
            quarry.setChunkPreLoaded(preForced);
        }
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        super.setPlacedBy(level, pos, state, entity, stack);
        if (!level.isClientSide && !this.disallowedDim().contains(level.dimension().location())) {
            if (level.getBlockEntity(pos) instanceof TileAdvQuarry quarry) {
                var enchantment = EnchantmentLevel.fromItem(stack);
                enchantment.sort(EnchantmentLevel.QUARRY_ENCHANTMENT_COMPARATOR);
                quarry.setEnchantments(enchantment);
                if (entity instanceof ServerPlayer player) {
                    quarry.workConfig = quarry.workConfig.noAutoStartConfig(); // Prevent machines from starting by being supplied enough energy to start. Wait for client setting.
                    PacketHandler.sendToClientPlayer(new AdvQuarryInitialMessage.Ask(pos, level.dimension()), player);
                }
            }
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : checkType(type, Holder.ADV_QUARRY_TYPE,
            CombinedBlockEntityTicker.of(
                this, level,
                PowerTile.getGenerator(),
                EnergyModuleItem.energyModuleTicker(),
                TileAdvQuarry::tick,
                PowerTile.logTicker(),
                MachineStorage.passItems(),
                MachineStorage.passFluid())
        );
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader world, BlockPos pos, Player player) {
        var stack = super.getCloneItemStack(state, target, world, pos, player);
        if (world.getBlockEntity(pos) instanceof TileAdvQuarry quarry) {
            EnchantedLootFunction.process(stack, quarry);
            ModuleLootFunction.process(stack, quarry);
        }
        return stack;
    }

    static Area findArea(Level world, BlockPos pos, Direction quarryBehind, Consumer<ItemStack> itemCollector) {
        return Stream.of(quarryBehind, quarryBehind.getCounterClockWise(), quarryBehind.getClockWise())
            .map(pos::relative)
            .map(world::getBlockEntity)
            .mapMulti(MapMulti.cast(QuarryMarker.class))
            .flatMap(m -> m.getArea().stream().peek(a -> m.removeAndGetItems().forEach(itemCollector)))
            .map(a -> a.assureY(4))
            .findFirst()
            .orElseGet(() -> createDefaultArea(pos, quarryBehind, QuarryPlus.config.common.chunkDestroyerLimit.get()));
    }

    @NotNull
    @VisibleForTesting
    static Area createDefaultArea(BlockPos pos, Direction quarryBehind, int limit) {
        var chunkPos = new ChunkPos(pos);
        final int minX, minZ, maxX, maxZ;
        if (0 < limit && limit < 16) {
            if (pos.getX() - limit / 2 < chunkPos.getMinBlockX()) {
                minX = chunkPos.getMinBlockX();
                maxX = minX + limit - 1;
            } else if (pos.getX() + limit / 2 > chunkPos.getMaxBlockX()) {
                maxX = chunkPos.getMaxBlockX();
                minX = maxX - limit + 1;
            } else {
                minX = pos.getX() - limit / 2;
                maxX = minX + limit - 1;
            }
            if (pos.getZ() - limit / 2 < chunkPos.getMinBlockZ()) {
                minZ = chunkPos.getMinBlockZ();
                maxZ = minZ + limit - 1;
            } else if (pos.getZ() + limit / 2 > chunkPos.getMaxBlockZ()) {
                maxZ = chunkPos.getMaxBlockZ();
                minZ = maxZ - limit + 1;
            } else {
                minZ = pos.getZ() - limit / 2;
                maxZ = minZ + limit - 1;
            }
        } else {
            minX = chunkPos.getMinBlockX();
            maxX = chunkPos.getMaxBlockX();
            minZ = chunkPos.getMinBlockZ();
            maxZ = chunkPos.getMaxBlockZ();
        }
        return new Area(
            minX - 1, pos.getY(), minZ - 1,
            maxX + 1, pos.getY() + 4, maxZ + 1, quarryBehind
        );
    }
}
