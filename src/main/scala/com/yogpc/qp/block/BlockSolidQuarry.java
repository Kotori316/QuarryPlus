package com.yogpc.qp.block;

import java.util.Optional;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.QuarryPlusI;
import com.yogpc.qp.compat.BuildcraftHelper;
import com.yogpc.qp.compat.InvUtils;
import com.yogpc.qp.tile.TileSolidQuarry;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import scala.Symbol;

public class BlockSolidQuarry extends ADismCBlock {
    public static final Symbol SYMBOL = Symbol.apply("SolidFuleQuarry");

    public BlockSolidQuarry() {
        super(Material.IRON, QuarryPlus.Names.solidquarry, ItemBlock::new);
        setHardness(1.5F);
        setResistance(10F);
        setSoundType(SoundType.STONE);
        setDefaultState(getBlockState().getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(ACTING, false));
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        if (!worldIn.isRemote) {
            EnumFacing facing = placer.getHorizontalFacing().getOpposite();
            worldIn.setBlockState(pos, state.withProperty(FACING, facing), 2);
            Optional.ofNullable((TileSolidQuarry) worldIn.getTileEntity(pos)).ifPresent(t -> {
                t.G_reinit();
                TileSolidQuarry.requestTicket.accept(t);
            });
        }
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, ACTING);
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
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
                                    EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = playerIn.getHeldItem(hand);
        if (InvUtils.isDebugItem(playerIn, hand)) return true;
        if (BuildcraftHelper.isWrench(playerIn, hand, stack, new RayTraceResult(new Vec3d(hitX, hitY, hitZ), facing, pos))) {
            Optional.ofNullable((TileSolidQuarry) worldIn.getTileEntity(pos)).ifPresent(TileSolidQuarry::G_reinit);
            return true;
        }
        if (!playerIn.isSneaking()) {
            playerIn.openGui(QuarryPlus.instance(), QuarryPlusI.guiIdSolidQuarry(), worldIn, pos.getX(), pos.getY(), pos.getZ());
            return true;
        }
        return false;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        boolean powered = state.getValue(ACTING);
        EnumFacing facing = state.getValue(FACING);
        return facing.getIndex() | (powered ? 8 : 0);
    }

    @SuppressWarnings("deprecation")
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(FACING, EnumFacing.getFront(meta & 7)).withProperty(ACTING, (meta & 8) == 8);
    }

    @Override
    protected boolean canRotate() {
        return true;
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileSolidQuarry();
    }
}
