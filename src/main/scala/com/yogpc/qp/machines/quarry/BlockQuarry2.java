package com.yogpc.qp.machines.quarry;

import java.util.Optional;
import java.util.function.Consumer;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.compat.BuildcraftHelper;
import com.yogpc.qp.machines.base.IEnchantableTile;
import com.yogpc.qp.machines.base.QPBlock;
import com.yogpc.qp.utils.Holder;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import static jp.t2v.lab.syntax.MapStreamSyntax.optCast;
import static net.minecraft.state.properties.BlockStateProperties.FACING;

public class BlockQuarry2 extends QPBlock {
    public BlockQuarry2() {
        super(Properties.create(Material.IRON)
            .hardnessAndResistance(1.5f, 10f)
            .sound(SoundType.STONE), QuarryPlus.Names.quarry2, ItemBlockEnchantable::new);
        setDefaultState(getStateContainer().getBaseState().with(FACING, EnumFacing.NORTH).with(QPBlock.WORKING(), false));
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        if (!worldIn.isRemote) {
            EnumFacing facing = placer.getHorizontalFacing().getOpposite();
            worldIn.setBlockState(pos, state.with(FACING, facing), 2);
            Consumer<TileQuarry2> consumer = IEnchantableTile.Util.initConsumer(stack);
            Optional.ofNullable(worldIn.getTileEntity(pos)).flatMap(optCast(TileQuarry2.class))
                .ifPresent(consumer.andThen(TileQuarry2.requestTicket));
        }
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, IBlockState> builder) {
        builder.add(FACING, QPBlock.WORKING());
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(IBlockReader worldIn) {
        return Holder.quarry2().create();
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (!worldIn.isRemote) {
            Optional.ofNullable(worldIn.getTileEntity(pos)).flatMap(optCast(TileQuarry2.class))
                .ifPresent(TileQuarry2::neighborChanged);
        }
    }

    @Override
    public boolean onBlockActivated(IBlockState state, World worldIn, BlockPos pos, EntityPlayer player,
                                    EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (super.onBlockActivated(state, worldIn, pos, player, hand, side, hitX, hitY, hitZ)) return true;
        ItemStack stack = player.getHeldItem(hand);
        if (BuildcraftHelper.isWrench(player, hand, stack, new RayTraceResult(new Vec3d(hitX, hitY, hitZ), side, pos))) {
            Optional.ofNullable((TileQuarry2) worldIn.getTileEntity(pos)).ifPresent(TileQuarry2::G_ReInit);
            return true;
        }
        if (!player.isSneaking()) {
            if (!worldIn.isRemote) {
                Optional.ofNullable((TileQuarry2) worldIn.getTileEntity(pos)).ifPresent(t ->
                    NetworkHooks.openGui(((EntityPlayerMP) player), new TileQuarry2.InteractionObject(t), pos)
                );
            }
            return true;
        }
        return false;
    }

    @Override
    public void onReplaced(IBlockState state, World worldIn, BlockPos pos, IBlockState newState, boolean isMoving) {
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
