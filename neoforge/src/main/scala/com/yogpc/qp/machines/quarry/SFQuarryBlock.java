package com.yogpc.qp.machines.quarry;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.integration.ftbchunks.FTBChunksProtectionCheck;
import com.yogpc.qp.integration.wrench.WrenchItems;
import com.yogpc.qp.machines.MachineStorage;
import com.yogpc.qp.machines.PowerTile;
import com.yogpc.qp.machines.QPBlock;
import com.yogpc.qp.utils.CombinedBlockEntityTicker;
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
import org.jetbrains.annotations.Nullable;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;

public final class SFQuarryBlock extends QPBlock implements EntityBlock {
    public static final String NAME = "solid_fuel_quarry";

    public SFQuarryBlock() {
        super(Properties.of()
            .mapColor(MapColor.METAL)
            .pushReaction(PushReaction.BLOCK)
            .strength(1.5f, 10f)
            .sound(SoundType.STONE), NAME);
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
            if (level.getBlockEntity(pos) instanceof SFQuarryEntity quarry) {
                quarry.setTileDataFromItem(null);
                var area = QuarryBlock.findArea(level, pos, facing.getOpposite(), quarry.storage::addItem);
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
                var preForced = QuarryChunkLoadUtil.makeChunkLoaded(level, pos, quarry.enabled);
                quarry.setChunkPreLoaded(preForced);
                quarry.sync();
            }
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
        if (!player.isShiftKeyDown()) {
            if (!world.isClientSide) {
                if (world.getBlockEntity(pos) instanceof SFQuarryEntity quarry) {
                    var stack = player.getItemInHand(hand);
                    if (WrenchItems.isWrenchItem(stack)) {
                        quarry.setState(QuarryState.WAITING, state);
                        quarry.target = null;
                    } else {
                        // Open Fuel Slot GUI
                        ScreenUtil.openScreen((ServerPlayer) player, quarry, pos);
                    }
                }
            }
            return InteractionResult.SUCCESS;
        }
        return super.use(state, world, pos, player, hand, hit);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return Holder.SOLID_FUEL_QUARRY_TYPE.create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : checkType(blockEntityType, Holder.SOLID_FUEL_QUARRY_TYPE,
            CombinedBlockEntityTicker.of(
                this, level,
                SFQuarryEntity::tickFuel,
                TileQuarry::tick,
                PowerTile.logTicker(),
                MachineStorage.passItems(),
                MachineStorage.passFluid())
        );
    }

}
