package com.yogpc.qp.machines.advquarry;

import java.util.function.Consumer;
import java.util.stream.Stream;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.Area;
import com.yogpc.qp.machines.EnchantedLootFunction;
import com.yogpc.qp.machines.EnchantmentLevel;
import com.yogpc.qp.machines.MachineStorage;
import com.yogpc.qp.machines.PowerTile;
import com.yogpc.qp.machines.QPBlock;
import com.yogpc.qp.machines.QuarryMarker;
import com.yogpc.qp.machines.module.EnergyModuleItem;
import com.yogpc.qp.machines.module.ModuleLootFunction;
import com.yogpc.qp.packet.ClientSyncMessage;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.utils.CombinedBlockEntityTicker;
import com.yogpc.qp.utils.MapMulti;
import com.yogpc.qp.utils.QuarryChunkLoadUtil;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
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
import net.minecraftforge.fmllegacy.network.NetworkHooks;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;

public class BlockAdvQuarry extends QPBlock implements EntityBlock {
    public static final String NAME = "adv_quarry";

    public BlockAdvQuarry() {
        super(Properties.of(Material.METAL)
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
                player.displayClientMessage(new TranslatableComponent("quarryplus.chat.disable_message", getName()), false);
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!player.isShiftKeyDown()) {
            if (!level.isClientSide && level.getBlockEntity(pos) instanceof TileAdvQuarry quarry) {
                NetworkHooks.openGui((ServerPlayer) player, quarry, pos);
            }
            return InteractionResult.SUCCESS;
        }
        return super.use(state, level, pos, player, hand, hit);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity entity, ItemStack stack) {
        super.setPlacedBy(level, pos, state, entity, stack);
        if (!level.isClientSide) {
            if (level.getBlockEntity(pos) instanceof TileAdvQuarry quarry) {
                Direction facing = state.getValue(FACING);
                var enchantment = EnchantmentLevel.fromItem(stack);
                enchantment.sort(EnchantmentLevel.QUARRY_ENCHANTMENT_COMPARATOR);
                quarry.setEnchantments(enchantment);
                quarry.area = findArea(level, pos, facing.getOpposite(), quarry.getStorage()::addItem);
                var preForced = QuarryChunkLoadUtil.makeChunkLoaded(level, pos, quarry.enabled);
                quarry.setChunkPreLoaded(preForced);
                PacketHandler.sendToClient(new ClientSyncMessage(quarry), level);
            }
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : checkType(type, Holder.ADV_QUARRY_TYPE,
            new CombinedBlockEntityTicker<>(
                PowerTile.getGenerator(),
                EnergyModuleItem.energyModuleTicker(),
                TileAdvQuarry::tick,
                PowerTile.logTicker(),
                MachineStorage.passItems(),
                MachineStorage.passFluid())
        );
    }

    @Override
    public ItemStack getPickBlock(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
        var stack = super.getPickBlock(state, target, world, pos, player);
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
            .orElseGet(() -> {
                var chunkPos = new ChunkPos(pos);
                return new Area(
                    chunkPos.getMinBlockX() - 1, pos.getY(), chunkPos.getMinBlockZ() - 1,
                    chunkPos.getMaxBlockX() + 1, pos.getY() + 4, chunkPos.getMaxBlockZ() + 1, quarryBehind
                );
            });
    }
}
