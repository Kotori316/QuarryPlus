package com.yogpc.qp.machines.quarry;

import java.util.Optional;
import java.util.function.Consumer;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.compat.BuildcraftHelper;
import com.yogpc.qp.machines.TranslationKeys;
import com.yogpc.qp.machines.base.IEnchantableTile;
import com.yogpc.qp.machines.base.QPBlock;
import com.yogpc.qp.machines.base.StatusContainer;
import com.yogpc.qp.machines.item.YSetterInteractionObject;
import com.yogpc.qp.utils.Holder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import static jp.t2v.lab.syntax.MapStreamSyntax.optCast;
import static net.minecraft.state.properties.BlockStateProperties.FACING;

public class BlockQuarry2 extends QPBlock {
    public BlockQuarry2() {
        super(Properties.create(Material.IRON)
            .hardnessAndResistance(1.5f, 10f)
            .sound(SoundType.STONE), QuarryPlus.Names.quarry2, BlockItemEnchantable::new);
        setDefaultState(getStateContainer().getBaseState().with(FACING, Direction.NORTH).with(QPBlock.WORKING(), false));
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        if (!worldIn.isRemote) {
            Direction facing = placer.getHorizontalFacing().getOpposite();
            worldIn.setBlockState(pos, state.with(FACING, facing), 2);
            Consumer<TileQuarry2> consumer = IEnchantableTile.Util.initConsumer(stack);
            Optional.ofNullable(worldIn.getTileEntity(pos)).flatMap(optCast(TileQuarry2.class))
                .ifPresent(consumer.andThen(TileQuarry2.requestTicket));
        }
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING, QPBlock.WORKING());
    }

    @Override
    public TileEntityType<? extends TileEntity> getTileType() {
        return Holder.quarry2();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos, isMoving);
        if (!worldIn.isRemote) {
            Optional.ofNullable(worldIn.getTileEntity(pos)).flatMap(optCast(TileQuarry2.class))
                .ifPresent(TileQuarry2::neighborChanged);
        }
    }

    @Override
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player,
                                    Hand hand, BlockRayTraceResult hit) {
        if (super.onBlockActivated(state, worldIn, pos, player, hand, hit)) return true;
        ItemStack stack = player.getHeldItem(hand);
        if (BuildcraftHelper.isWrench(player, hand, stack, hit)) {
            if (!worldIn.isRemote)
                Optional.ofNullable((TileQuarry2) worldIn.getTileEntity(pos)).ifPresent(tileQuarry2 -> tileQuarry2.onActivated(player));
            return true;
        }
        if (!player.isSneaking()) {
            if (!worldIn.isRemote) {
                Optional.ofNullable((TileQuarry2) worldIn.getTileEntity(pos)).map(t -> {
                    if (stack.getItem() == Holder.itemYSetter()) return YSetterInteractionObject.apply(t, pos);
                    else if (stack.getItem() == Holder.itemStatusChecker())
                        return new StatusContainer.ContainerProvider(pos);
                    else return new ContainerQuarryModule.InteractionObject(pos, TranslationKeys.quarry2);
                }).ifPresent(o -> NetworkHooks.openGui(((ServerPlayerEntity) player), o, pos));
            }
            return true;
        }
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            if (!worldIn.isRemote) {
                TileEntity entity = worldIn.getTileEntity(pos);
                if (entity instanceof TileQuarry2) {
                    TileQuarry2 inventory = (TileQuarry2) entity;
                    InventoryHelper.dropInventoryItems(worldIn, pos, inventory.moduleInv());
                    worldIn.updateComparatorOutputLevel(pos, state.getBlock());
                }
            }
            super.onReplaced(state, worldIn, pos, newState, isMoving);
        }
    }

}
