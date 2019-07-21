package com.yogpc.qp.block;

import java.util.Optional;
import java.util.function.Consumer;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.QuarryPlusI;
import com.yogpc.qp.compat.BuildcraftHelper;
import com.yogpc.qp.item.ItemBlockEnchantable;
import com.yogpc.qp.tile.IEnchantableTile;
import com.yogpc.qp.tile.TileQuarry2;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import static jp.t2v.lab.syntax.MapStreamSyntax.optCast;

public class BlockQuarry2 extends ADismCBlock {
    public BlockQuarry2() {
        super(Material.IRON, QuarryPlus.Names.quarry2, ItemBlockEnchantable::new);
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
            Consumer<TileQuarry2> consumer = IEnchantableTile.Util.initConsumer(stack);
            Optional.ofNullable(worldIn.getTileEntity(pos)).flatMap(optCast(TileQuarry2.class))
                .ifPresent(consumer.andThen(TileQuarry2.requestTicket));
        }
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
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player,
                                    EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (super.onBlockActivated(worldIn, pos, state, player, hand, side, hitX, hitY, hitZ)) return true;
        ItemStack stack = player.getHeldItem(hand);
        if (BuildcraftHelper.isWrench(player, hand, stack, new RayTraceResult(new Vec3d(hitX, hitY, hitZ), side, pos))) {
            if (!worldIn.isRemote)
                Optional.ofNullable((TileQuarry2) worldIn.getTileEntity(pos)).ifPresent(tileQuarry2 -> tileQuarry2.onActivated(player));
            return true;
        }
        if (!player.isSneaking()) {
//            if (!worldIn.isRemote) {
//                Optional.ofNullable((TileQuarry2) worldIn.getTileEntity(pos)).ifPresent(t ->
//                    NetworkHooks.openGui(((EntityPlayerMP) player), new TileQuarry2.InteractionObject(t), pos)
//                );
//            }
            player.openGui(QuarryPlus.instance(), QuarryPlusI.guiIdQuarry2(), worldIn, pos.getX(), pos.getY(), pos.getZ());
            return true;
        }
        return false;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        if (!worldIn.isRemote) {
            TileEntity entity = worldIn.getTileEntity(pos);
            if (entity instanceof TileQuarry2) {
                TileQuarry2 inventory = (TileQuarry2) entity;
                InventoryHelper.dropInventoryItems(worldIn, pos, inventory.moduleInv());
                worldIn.updateComparatorOutputLevel(pos, state.getBlock());
            }
        }
        super.breakBlock(worldIn, pos, state);
    }


    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING, ACTING);
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

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileQuarry2();
    }

    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest) {
        return willHarvest || super.removedByPlayer(state, world, pos, player, false);
    }

    @Override
    public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, @Nullable TileEntity te, ItemStack stack) {
        super.harvestBlock(worldIn, player, pos, state, te, stack);
        worldIn.setBlockToAir(pos);
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        TileEntity entity = world.getTileEntity(pos);
        if (entity instanceof TileQuarry2) {
            TileQuarry2 quarry = (TileQuarry2) entity;
            ItemStack stack = new ItemStack(itemBlock(), 1, 0);
            IEnchantableTile.Util.enchantmentToIS(quarry, stack);
            drops.add(stack);
        }
    }

}
