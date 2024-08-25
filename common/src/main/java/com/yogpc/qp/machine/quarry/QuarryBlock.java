package com.yogpc.qp.machine.quarry;

import com.yogpc.qp.machine.*;
import com.yogpc.qp.machine.marker.QuarryMarker;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
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

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

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
            QuarryEntity::serverTick,
            (l, p, s, e) -> e.storage.passItems(l, p),
            (l, p, s, e) -> e.storage.passFluids(l, p)
        ));
    }

    // Action
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        var entity = this.<QuarryEntity>getBlockEntityType().map(t -> t.getBlockEntity(level, pos)).orElse(null);
        if (entity != null) {
            if (!level.isClientSide()) {
                if (entity.enabled) {
                    openGui((ServerPlayer) player, level, pos, entity);
                } else {
                    player.displayClientMessage(Component.translatable("quarryplus.chat.disable_message", getName()), true);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    protected abstract void openGui(ServerPlayer player, Level level, BlockPos pos, QuarryEntity quarry);

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide) {
            if (level.getBlockEntity(pos) instanceof QuarryEntity quarry) {
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
                            var base = pos.relative(facing.getOpposite());
                            var corner1 = base.relative(facing.getClockWise(), 5).above(4);
                            var corner2 = base.relative(facing.getCounterClockWise(), 5).relative(facing.getOpposite(), 10);
                            var area = new Area(corner1, corner2, facing.getOpposite());
                            return new QuarryMarker.StaticLink(area);
                        });
                    quarry.setArea(assumeY(markerLink.area()));
                    markerLink.drops().forEach(quarry.storage::addItem);
                    markerLink.remove(level);
                }
                quarry.setState(QuarryState.WAITING, state);
            }
        }
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        var stack = super.getCloneItemStack(level, pos, state);
        this.<QuarryEntity>getBlockEntityType().map(t -> t.getBlockEntity(level, pos))
            .ifPresent(e -> e.saveToItem(stack, level.registryAccess()));
        return stack;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            // Logic from Containers.dropContentsOnDestroy()
            if (level.getBlockEntity(pos) instanceof QuarryEntity entity) {
                Containers.dropContents(level, pos, entity.moduleInventory);
                level.updateNeighbourForOutputSignal(pos, state.getBlock());
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        var tag = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (tag != null && tag.contains("storage")) {
            tooltipComponents.add(Component.literal("DON'T PLACE THIS INTO WORLD.").withStyle(ChatFormatting.RED));
            tooltipComponents.add(Component.literal("This block might cause crash.").withStyle(ChatFormatting.RED));
        }
    }

    @Override
    public Stream<ItemStack> creativeTabItem(CreativeModeTab.ItemDisplayParameters parameters) {
        Stream.Builder<ItemStack> builder = Stream.builder();
        var stack = new ItemStack(this);
        builder.add(stack);

        var lookup = parameters.holders().lookupOrThrow(Registries.ENCHANTMENT);
        var efficiency = lookup.getOrThrow(Enchantments.EFFICIENCY);
        var unbreaking = lookup.getOrThrow(Enchantments.UNBREAKING);
        var fortune = lookup.getOrThrow(Enchantments.FORTUNE);
        var silkTouch = lookup.getOrThrow(Enchantments.SILK_TOUCH);
        {
            // Efficiency
            var e = stack.copy();
            e.enchant(efficiency, 5);
            builder.add(e);
        }
        {
            // E, U, F
            var e = stack.copy();
            e.enchant(efficiency, 5);
            e.enchant(unbreaking, 3);
            e.enchant(fortune, 3);
            builder.add(e);
        }
        {
            // E, U, S
            var e = stack.copy();
            e.enchant(efficiency, 5);
            e.enchant(unbreaking, 3);
            e.enchant(silkTouch, 1);
            builder.add(e);
        }
        return builder.build();
    }

    static Area assumeY(Area area) {
        int distanceY = area.maxY() - area.minY();
        if (distanceY >= 4) {
            return area;
        }
        return new Area(
            area.minX(), area.minY(), area.minZ(),
            area.maxX(), area.minY() + 4, area.maxZ(),
            area.direction()
        );
    }
}
