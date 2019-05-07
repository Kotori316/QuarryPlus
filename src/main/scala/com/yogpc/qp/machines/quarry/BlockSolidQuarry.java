package com.yogpc.qp.machines.quarry;

import java.util.Optional;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.compat.BuildcraftHelper;
import com.yogpc.qp.machines.base.QPBlock;
import com.yogpc.qp.utils.Holder;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBlock;
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
import scala.Symbol;

import static net.minecraft.state.properties.BlockStateProperties.FACING;

public class BlockSolidQuarry extends QPBlock {
    public static final Symbol SYMBOL = Symbol.apply("SolidFuelQuarry");

    public BlockSolidQuarry() {
        super(Block.Properties.create(Material.IRON)
            .hardnessAndResistance(1.5f, 10f)
            .sound(SoundType.STONE), QuarryPlus.Names.solidquarry, ItemBlock::new);
        setDefaultState(getStateContainer().getBaseState().with(FACING, EnumFacing.NORTH).with(QPBlock.WORKING(), false));
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        if (!worldIn.isRemote) {
            EnumFacing facing = placer.getHorizontalFacing().getOpposite();
            worldIn.setBlockState(pos, state.with(FACING, facing), 2);
            Optional.ofNullable((TileSolidQuarry) worldIn.getTileEntity(pos)).ifPresent(t -> {
                t.G_ReInit();
                TileSolidQuarry.requestTicket.accept(t);
            });
        }
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, IBlockState> builder) {
        builder.add(FACING, QPBlock.WORKING());
    }

    @Override
    @SuppressWarnings("deprecation")
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
        if (!worldIn.isRemote) {
            Optional.ofNullable((TileSolidQuarry) worldIn.getTileEntity(pos)).ifPresent(TileSolidQuarry::G_renew_powerConfigure);
        }
    }

    @Override
    public boolean onBlockActivated(IBlockState state, World worldIn, BlockPos pos, EntityPlayer playerIn,
                                    EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (super.onBlockActivated(state, worldIn, pos, playerIn, hand, side, hitX, hitY, hitZ)) return true;
        ItemStack stack = playerIn.getHeldItem(hand);
        if (BuildcraftHelper.isWrench(playerIn, hand, stack, new RayTraceResult(new Vec3d(hitX, hitY, hitZ), side, pos))) {
            Optional.ofNullable((TileSolidQuarry) worldIn.getTileEntity(pos)).ifPresent(TileSolidQuarry::G_ReInit);
            return true;
        }
        if (!playerIn.isSneaking()) {
            if (!worldIn.isRemote) {
                Optional.ofNullable((TileSolidQuarry) worldIn.getTileEntity(pos)).ifPresent(t ->
                    NetworkHooks.openGui(((EntityPlayerMP) playerIn), t, pos)
                );
            }
            return true;
        }
        return false;
    }

    @Override
    public TileEntity createNewTileEntity(IBlockReader worldIn) {
        return Holder.solidQuarryType().create();
    }
}
