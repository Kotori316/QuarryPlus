package com.yogpc.qp.machines.advquarry;

import java.util.Optional;
import java.util.function.Consumer;

import com.yogpc.qp.Config;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.compat.BuildcraftHelper;
import com.yogpc.qp.machines.base.IEnchantableTile;
import com.yogpc.qp.machines.base.QPBlock;
import com.yogpc.qp.machines.item.YSetterInteractionObject;
import com.yogpc.qp.machines.quarry.BlockItemEnchantable;
import com.yogpc.qp.utils.Holder;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import static net.minecraft.state.properties.BlockStateProperties.FACING;

public class BlockAdvQuarry extends QPBlock {

    public BlockAdvQuarry() {
        super(Properties.create(Material.IRON)
            .hardnessAndResistance(1.5f, 10f)
            .sound(SoundType.STONE), QuarryPlus.Names.advquarry, BlockItemEnchantable::new);
        setDefaultState(getStateContainer().getBaseState().with(FACING, Direction.NORTH).with(QPBlock.WORKING(), false));
    }

    @Override
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (super.onBlockActivated(state, worldIn, pos, player, hand, hit)) return true;
        ItemStack stack = player.getHeldItem(hand);
        if (BuildcraftHelper.isWrench(player, hand, stack, hit)) {
            if (!worldIn.isRemote) {
                TileAdvQuarry quarry = (TileAdvQuarry) worldIn.getTileEntity(pos);
                if (quarry != null) {
                    if (stack.getItem() == Items.STICK) {
                        if (Config.common().noEnergy().get())
                            quarry.stickActivated(player);
                    } else {
                        quarry.G_ReInit();
                        if (Config.common().noEnergy().get()) {
                            quarry.stickActivated(player);
                        }
                    }
                }
            }
            return true;
        } else if (stack.getItem() == Holder.itemStatusChecker()) {
            if (!worldIn.isRemote)
                Optional.ofNullable((IEnchantableTile) worldIn.getTileEntity(pos)).ifPresent(t ->
                    t.sendEnchantMassage(player));
            return true;
        } else if (stack.getItem() == Holder.itemLiquidSelector()) {
            // Not implemented.
            return true;
        } else if (stack.getItem() == Holder.itemYSetter()) {
            if (!worldIn.isRemote)
                NetworkHooks.openGui(((ServerPlayerEntity) player), YSetterInteractionObject.apply((TileAdvQuarry) worldIn.getTileEntity(pos), pos), pos);
            return true;
        } else if (!player.isSneaking()) {
            if (!worldIn.isRemote)
                NetworkHooks.openGui(((ServerPlayerEntity) player), (TileAdvQuarry) worldIn.getTileEntity(pos), pos);
            return true;
        }
        return false;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        if (!worldIn.isRemote) {
            Direction facing = Optional.ofNullable(placer).map(Entity::getHorizontalFacing).map(Direction::getOpposite).orElse(Direction.NORTH);
            worldIn.setBlockState(pos, state.with(FACING, facing), 2);
            Consumer<TileAdvQuarry> consumer = IEnchantableTile.Util.initConsumer(stack);
            Optional.ofNullable((TileAdvQuarry) worldIn.getTileEntity(pos)).ifPresent(consumer.andThen(TileAdvQuarry.requestTicket));
        }
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING, QPBlock.WORKING());
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean b) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos, b);
        if (!worldIn.isRemote)
            Optional.ofNullable((TileAdvQuarry) worldIn.getTileEntity(pos)).ifPresent(TileAdvQuarry::neighborChanged);
    }

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        super.fillItemGroup(group, items);
    }

    @Override
    public TileEntityType<? extends TileEntity> getTileType() {
        return Holder.advQuarryType();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            if (!worldIn.isRemote) {
                TileEntity entity = worldIn.getTileEntity(pos);
                if (entity instanceof TileAdvQuarry) {
                    TileAdvQuarry inventory = (TileAdvQuarry) entity;
                    InventoryHelper.dropInventoryItems(worldIn, pos, inventory.moduleInv());
                    worldIn.updateComparatorOutputLevel(pos, state.getBlock());
                }
            }
            super.onReplaced(state, worldIn, pos, newState, isMoving);
        }
    }
}
