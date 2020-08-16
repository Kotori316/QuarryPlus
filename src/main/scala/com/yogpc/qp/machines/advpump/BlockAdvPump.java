package com.yogpc.qp.machines.advpump;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.yogpc.qp.Config;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.compat.BuildcraftHelper;
import com.yogpc.qp.machines.base.IEnchantableTile;
import com.yogpc.qp.machines.base.QPBlock;
import com.yogpc.qp.machines.base.StatusContainer;
import com.yogpc.qp.utils.Holder;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.StateContainer;
import net.minecraft.tags.FluidTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class BlockAdvPump extends QPBlock {
    public BlockAdvPump() {
        super(Properties.create(Material.IRON)
            .hardnessAndResistance(1.5f, 10f)
            .sound(SoundType.STONE), QuarryPlus.Names.advpump, BlockItemAdvPump::new);
        setDefaultState(getStateContainer().getBaseState().with(QPBlock.WORKING(), false));
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit) {
        if (super.onBlockActivated(state, worldIn, pos, playerIn, hand, hit).isSuccess())
            return ActionResultType.SUCCESS;
        ItemStack stack = playerIn.getHeldItem(hand);
        if (Config.common().debug() && stack.getItem() == Items.STICK) {
            Optional.ofNullable((TileAdvPump) worldIn.getTileEntity(pos)).ifPresent(TileAdvPump::toggleDelete);
            playerIn.sendStatusMessage(new StringTextComponent("Changed delete mode."), false);
            return ActionResultType.SUCCESS;
        } else if (BuildcraftHelper.isWrench(playerIn, hand, stack, hit)) {
            if (!worldIn.isRemote) {
                Optional.ofNullable((TileAdvPump) worldIn.getTileEntity(pos)).ifPresent(TileAdvPump::G_ReInit);
            }
            return ActionResultType.SUCCESS;
        } else if (stack.getItem() == Holder.itemStatusChecker()) {
            if (!worldIn.isRemote)
                NetworkHooks.openGui(((ServerPlayerEntity) playerIn), new StatusContainer.ContainerProvider(pos), pos);
            return ActionResultType.SUCCESS;
        } else if (!playerIn.isCrouching()) {
            if (!worldIn.isRemote) {
                NetworkHooks.openGui(((ServerPlayerEntity) playerIn), (TileAdvPump) worldIn.getTileEntity(pos), pos);
            }
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.PASS;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        if (!worldIn.isRemote) {
            Consumer<TileAdvPump> consumer = IEnchantableTile.Util.initConsumer(stack);
            Consumer<TileAdvPump> deleteSetter = pump ->
                Stream.iterate(pos.down(), BlockPos::down)
                    .filter(p -> !worldIn.isAirBlock(p) || p.getY() < 0)
                    .findFirst()
                    .map(worldIn::getFluidState)
                    .map(f -> f.getFluid().isIn(FluidTags.WATER))
                    .ifPresent(pump::delete_$eq);
            Optional.ofNullable((TileAdvPump) worldIn.getTileEntity(pos)).ifPresent(consumer.andThen(deleteSetter).andThen(TileAdvPump.requestTicket));
        }
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(QPBlock.WORKING());
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
//        tooltip.add(new TranslationTextComponent(TranslationKeys.TOOLTIP_ADVPUMP, ' '));
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            if (!worldIn.isRemote) {
                TileEntity entity = worldIn.getTileEntity(pos);
                if (entity instanceof TileAdvPump) {
                    TileAdvPump inventory = (TileAdvPump) entity;
                    InventoryHelper.dropInventoryItems(worldIn, pos, inventory.moduleInv());
                    worldIn.updateComparatorOutputLevel(pos, state.getBlock());
                }
            }
            super.onReplaced(state, worldIn, pos, newState, isMoving);
        }
    }

    @Override
    public TileEntityType<? extends TileEntity> getTileType() {
        return Holder.advPumpType();
    }
}
