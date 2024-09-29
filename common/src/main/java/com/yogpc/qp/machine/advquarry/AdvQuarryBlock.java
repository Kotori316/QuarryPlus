package com.yogpc.qp.machine.advquarry;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.machine.*;
import com.yogpc.qp.machine.marker.QuarryMarker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.ChunkPos;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.util.List;
import java.util.stream.Stream;

import static com.yogpc.qp.machine.QpBlockProperty.WORKING;
import static net.minecraft.world.level.block.state.properties.BlockStateProperties.FACING;

public class AdvQuarryBlock extends QpEntityBlock {
    public static final String NAME = "adv_quarry";

    public AdvQuarryBlock() {
        super(Properties.of()
            .mapColor(MapColor.METAL)
            .pushReaction(PushReaction.BLOCK)
            .strength(1.5f, 10f)
            .sound(SoundType.STONE), NAME, AdvQuarryItem::new);
        registerDefaultState(getStateDefinition().any()
            .setValue(WORKING, false)
            .setValue(FACING, Direction.NORTH));
    }

    @Override
    protected QpBlock createBlock(Properties properties) {
        return new AdvQuarryBlock();
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

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) {
            return null;
        }
        return createTickerHelper(blockEntityType, this.<AdvQuarryEntity>getBlockEntityType().orElse(null), CombinedBlockEntityTicker.of(this, level,
            PowerEntity.logTicker(),
            AdvQuarryEntity::serverTick,
            (l, p, s, e) -> e.storage.passItems(l, p),
            (l, p, s, e) -> e.storage.passFluids(l, p)
        ));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        var entity = this.<AdvQuarryEntity>getBlockEntityType().map(t -> t.getBlockEntity(level, pos)).orElse(null);
        if (entity != null) {
            if (!level.isClientSide()) {
                if (entity.enabled) {
                    PlatformAccess.getAccess().openGui((ServerPlayer) player, new GeneralScreenHandler<>(entity, AdvQuarryContainer::new));
                } else {
                    player.displayClientMessage(Component.translatable("quarryplus.chat.disable_message", getName()), true);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.getBlockEntity(pos) instanceof AdvQuarryEntity quarry) {
            if (!level.isClientSide) {
                var facing = state.getValue(BlockStateProperties.FACING);
                {
                    var markerLink = Stream.of(facing.getOpposite(), facing.getCounterClockWise(), facing.getClockWise())
                        .map(pos::relative)
                        .map(level::getBlockEntity)
                        .filter(QuarryMarker.class::isInstance)
                        .map(QuarryMarker.class::cast)
                        .flatMap(m -> m.getLink().stream())
                        .findAny()
                        .orElseGet(() -> {
                            // set initial area
                            return new QuarryMarker.StaticLink(createDefaultArea(pos, facing, 0));
                        });
                    quarry.setArea(Area.assumeY(markerLink.area()));
                    markerLink.drops().forEach(quarry.storage::addItem);
                    markerLink.remove(level);
                }
                if (placer instanceof ServerPlayer serverPlayer) {
                    quarry.workConfig = quarry.workConfig.noAutoStartConfig();
                    PlatformAccess.getAccess().packetHandler().sendToClientPlayer(new AdvQuarryInitialAskMessage(quarry), serverPlayer);
                }
                quarry.setState(AdvQuarryState.WAITING, state);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.literal("WIP"));
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
