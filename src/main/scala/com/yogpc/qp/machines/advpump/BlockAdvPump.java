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
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.state.StateManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class BlockAdvPump extends MachineBlock {
    public static final String NAME = "adv_pump";
    public final BlockItem blockItem;

    public BlockAdvPump() {
        super(FabricBlockSettings.of(Material.METAL)
            .strength(1.5f, 10f)
            .sounds(BlockSoundGroup.STONE)
            .breakByTool(FabricToolTags.PICKAXES));
        setDefaultState(getStateManager().getDefaultState()
            .with(WORKING, false));
        blockItem = new ItemAdvPump(this, new FabricItemSettings().group(QuarryPlus.CREATIVE_TAB).fireproof());
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return QuarryPlus.ModObjects.ADV_PUMP_TYPE.instantiate(pos, state);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(WORKING);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return world.isClient ? null : checkType(type, QuarryPlus.ModObjects.ADV_PUMP_TYPE,
            new CombinedBlockEntityTicker<>(PowerTile.getGenerator(), TileAdvPump::tick, PowerTile.logTicker(), MachineStorage.passFluid()));
    }

    @Override
    @SuppressWarnings("deprecation")
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        var stack = player.getStackInHand(hand);
        if (stack.getItem() == Items.STICK) {
            if (!world.isClient) {
                world.getBlockEntity(pos, QuarryPlus.ModObjects.ADV_PUMP_TYPE)
                    .ifPresent(TileAdvPump::reset);
            }
            return ActionResult.SUCCESS;
        }
        return super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (!world.isClient) {
            if (world.getBlockEntity(pos) instanceof TileAdvPump pump) {
                var preForced = QuarryChunkLoadUtil.makeChunkLoaded(world, pos);
                pump.setEnchantment(EnchantmentEfficiency.fromMap(EnchantmentHelper.get(itemStack)));
                pump.setChunkPreLoaded(preForced);
            }
        }
    }

    @Override
    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        // Called in client.
        var stack = super.getPickStack(world, pos, state);
        if (world.getBlockEntity(pos) instanceof TileAdvPump pump) {
            EnchantedLootFunction.process(stack, pump);
        }
        return stack;
    }

}
