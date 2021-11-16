package com.yogpc.qp.machines.quarry;

import java.util.function.Consumer;
import java.util.stream.Stream;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.Area;
import com.yogpc.qp.machines.EnchantedLootFunction;
import com.yogpc.qp.machines.MachineStorage;
import com.yogpc.qp.machines.PowerTile;
import com.yogpc.qp.machines.QPBlock;
import com.yogpc.qp.machines.QuarryMarker;
import com.yogpc.qp.utils.CombinedBlockEntityTicker;
import com.yogpc.qp.utils.QuarryChunkLoadUtil;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;

public class BlockQuarry extends QPBlock implements EntityBlock {
    public static final String NAME = "quarry";

    public BlockQuarry() {
        super(FabricBlockSettings.of(Material.METAL)
            .strength(1.5f, 10f)
            .sounds(SoundType.STONE)
            .requiresTool(), NAME, ItemQuarry::new);
        registerDefaultState(getStateDefinition().any()
            .setValue(BlockStateProperties.FACING, Direction.NORTH)
            .setValue(WORKING, false));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return QuarryPlus.ModObjects.QUARRY_TYPE.create(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return world.isClientSide ? null : createTickerHelper(type, QuarryPlus.ModObjects.QUARRY_TYPE,
            new CombinedBlockEntityTicker<>(PowerTile.getGenerator(), TileQuarry::tick, PowerTile.logTicker(), MachineStorage.passItems(), MachineStorage.passFluid()));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BlockStateProperties.FACING, WORKING);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @javax.annotation.Nullable LivingEntity entity, ItemStack stack) {
        super.setPlacedBy(level, pos, state, entity, stack);
        if (!level.isClientSide) {
            Direction facing = entity == null ? Direction.NORTH : entity.getDirection().getOpposite();
            if (level.getBlockEntity(pos) instanceof TileQuarry quarry) {
                quarry.setEnchantments(EnchantmentHelper.getEnchantments(stack));
                quarry.setTileDataFromItem(stack.getTagElement(BlockItem.BLOCK_ENTITY_TAG));
                var area = findArea(level, pos, facing.getOpposite(), quarry.storage::addItem);
                if (area.maxX() - area.minX() > 1 && area.maxZ() - area.minZ() > 1) {
                    quarry.setState(QuarryState.WAITING, state.setValue(BlockStateProperties.FACING, facing));
                    quarry.setArea(area);
                } else {
                    if (entity instanceof Player player)
                        player.displayClientMessage(new TranslatableComponent("quarryplus.chat.warn_area"), false);
                }
                var preForced = QuarryChunkLoadUtil.makeChunkLoaded(level, pos);
                quarry.setChunkPreLoaded(preForced);
            }
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        var stack = player.getItemInHand(hand);
        if (stack.getItem() == Items.STICK) {
            if (!world.isClientSide) {
                if (world.getBlockEntity(pos) instanceof TileQuarry quarry) {
                    quarry.setState(QuarryState.WAITING, state);
                    quarry.target = null;
                }
            }
            return InteractionResult.SUCCESS;
        }
        return super.use(state, world, pos, player, hand, hit);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter world, BlockPos pos, BlockState state) {
        // Called in client.
        var stack = super.getCloneItemStack(world, pos, state);
        if (world.getBlockEntity(pos) instanceof TileQuarry quarry) {
            QuarryLootFunction.process(stack, quarry);
            EnchantedLootFunction.process(stack, quarry);
        }
        return stack;
    }

    static Area findArea(Level world, BlockPos pos, Direction quarryBehind, Consumer<ItemStack> itemCollector) {
        return Stream.of(quarryBehind, quarryBehind.getCounterClockWise(), quarryBehind.getClockWise())
            .map(pos::relative)
            .flatMap(p -> {
                if (world.getBlockEntity(p) instanceof QuarryMarker marker) return Stream.of(marker);
                else return Stream.empty();
            })
            .flatMap(m -> m.getArea().stream().peek(a -> m.removeAndGetItems().forEach(itemCollector)))
            .findFirst()
            .map(a -> a.assureY(4))
            .orElse(new Area(pos.relative(quarryBehind).relative(quarryBehind.getCounterClockWise(), 5),
                pos.relative(quarryBehind, 11).relative(quarryBehind.getClockWise(), 5).relative(Direction.UP, 4), quarryBehind));
    }
}
