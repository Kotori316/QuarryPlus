package com.yogpc.qp.machines.advpump;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.EnchantedLootFunction;
import com.yogpc.qp.machines.MachineBlock;
import com.yogpc.qp.machines.MachineStorage;
import com.yogpc.qp.machines.PowerTile;
import com.yogpc.qp.utils.CombinedBlockEntityTicker;
import com.yogpc.qp.utils.QuarryChunkLoadUtil;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class BlockAdvPump extends MachineBlock {
    public static final String NAME = "adv_pump";
    public final BlockItem blockItem;

    public BlockAdvPump() {
        super(FabricBlockSettings.of(Material.METAL)
            .strength(1.5f, 10f)
            .sounds(SoundType.STONE)
            .breakByTool(FabricToolTags.PICKAXES));
        registerDefaultState(getStateDefinition().any()
            .setValue(WORKING, false));
        blockItem = new ItemAdvPump(this, new FabricItemSettings().tab(QuarryPlus.CREATIVE_TAB).fireResistant());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return QuarryPlus.ModObjects.ADV_PUMP_TYPE.create(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(WORKING);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return world.isClientSide ? null : createTickerHelper(type, QuarryPlus.ModObjects.ADV_PUMP_TYPE,
            new CombinedBlockEntityTicker<>(PowerTile.getGenerator(), TileAdvPump::tick, PowerTile.logTicker(), MachineStorage.passFluid()));
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        var stack = player.getItemInHand(hand);
        if (stack.getItem() == Items.STICK) {
            if (!world.isClientSide) {
                world.getBlockEntity(pos, QuarryPlus.ModObjects.ADV_PUMP_TYPE)
                    .ifPresent(TileAdvPump::reset);
            }
            return InteractionResult.SUCCESS;
        }
        return super.use(state, world, pos, player, hand, hit);
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.setPlacedBy(world, pos, state, placer, itemStack);
        if (!world.isClientSide) {
            if (world.getBlockEntity(pos) instanceof TileAdvPump pump) {
                var preForced = QuarryChunkLoadUtil.makeChunkLoaded(world, pos);
                pump.setEnchantment(EnchantmentEfficiency.fromMap(EnchantmentHelper.getEnchantments(itemStack)));
                pump.setChunkPreLoaded(preForced);
            }
        }
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter world, BlockPos pos, BlockState state) {
        // Called in client.
        var stack = super.getCloneItemStack(world, pos, state);
        if (world.getBlockEntity(pos) instanceof TileAdvPump pump) {
            EnchantedLootFunction.process(stack, pump);
        }
        return stack;
    }

}
