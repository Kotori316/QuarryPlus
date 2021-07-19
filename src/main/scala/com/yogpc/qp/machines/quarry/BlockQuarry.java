package com.yogpc.qp.machines.quarry;

import java.util.function.Consumer;
import java.util.stream.Stream;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.Area;
import com.yogpc.qp.machines.EnchantedLootFunction;
import com.yogpc.qp.machines.MachineStorage;
import com.yogpc.qp.machines.QuarryMarker;
import com.yogpc.qp.utils.CombinedBlockEntityTicker;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class BlockQuarry extends BlockWithEntity {
    public static final String NAME = "quarry";
    public static final BooleanProperty WORKING = BooleanProperty.of("working");
    public final BlockItem blockItem = new ItemQuarry(this, new FabricItemSettings().group(QuarryPlus.CREATIVE_TAB).fireproof());

    public BlockQuarry() {
        super(FabricBlockSettings.of(Material.METAL)
            .strength(1.5f, 10f)
            .sounds(BlockSoundGroup.STONE)
            .breakByTool(FabricToolTags.PICKAXES));
        setDefaultState(getStateManager().getDefaultState()
            .with(Properties.FACING, Direction.NORTH)
            .with(WORKING, false));
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return QuarryPlus.ModObjects.QUARRY_TYPE.instantiate(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient ? null : checkType(type, QuarryPlus.ModObjects.QUARRY_TYPE,
            new CombinedBlockEntityTicker<>(TileQuarry.GENERATE_ENERGY, TileQuarry::tick, MachineStorage.passItems()));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(Properties.FACING, WORKING);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (!world.isClient) {
            var facing = placer != null ? placer.getHorizontalFacing().getOpposite() : Direction.NORTH;
            world.setBlockState(pos, state.with(Properties.FACING, facing), 2);
            if (world.getBlockEntity(pos) instanceof TileQuarry quarry) {
                quarry.setEnchantments(EnchantmentHelper.get(itemStack));
                quarry.setTileDataFromItem(itemStack.getSubNbt(BlockItem.BLOCK_ENTITY_TAG_KEY));
                var area = findArea(world, pos, facing.getOpposite(), quarry.storage::addItem);
                if (area.maxX() - area.minX() > 1 && area.maxZ() - area.minZ() > 1) {
                    quarry.setState(QuarryState.WAITING, state.with(Properties.FACING, facing));
                    quarry.setArea(area);
                } else {
                    if (placer instanceof PlayerEntity player)
                        player.sendMessage(new TranslatableText("quarryplus.chat.warn_area"), false);
                }
            }
        }
    }

    @Override
    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        // Called in client.
        var stack = super.getPickStack(world, pos, state);
        if (world.getBlockEntity(pos) instanceof TileQuarry quarry) {
            QuarryLootFunction.process(stack, quarry);
            EnchantedLootFunction.process(stack, quarry);
        }
        return stack;
    }

    static Area findArea(World world, BlockPos pos, Direction quarryBehind, Consumer<ItemStack> itemCollector) {
        return Stream.of(quarryBehind, quarryBehind.rotateYCounterclockwise(), quarryBehind.rotateYClockwise())
            .map(pos::offset)
            .flatMap(p -> {
                if (world.getBlockEntity(p) instanceof QuarryMarker marker) return Stream.of(marker);
                else return Stream.empty();
            })
            .flatMap(m -> m.getArea().stream().peek(a -> m.removeAndGetItems().forEach(itemCollector)))
            .findFirst()
            .orElse(new Area(pos.offset(quarryBehind).offset(quarryBehind.rotateYCounterclockwise(), 5),
                pos.offset(quarryBehind, 11).offset(quarryBehind.rotateYClockwise(), 5).offset(Direction.UP, 4), quarryBehind.getOpposite()));
    }
}
