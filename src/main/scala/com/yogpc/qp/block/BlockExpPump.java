package com.yogpc.qp.block;

import java.util.List;
import java.util.Optional;

import com.yogpc.qp.NonNullList;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.item.ItemBlockPump;
import com.yogpc.qp.tile.IEnchantableTile;
import com.yogpc.qp.tile.TileExpPump;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import scala.Symbol;

import static com.yogpc.qp.block.BlockPump.CONNECTED;

public class BlockExpPump extends ADismCBlock {

    public static final Symbol SYMBOL = Symbol.apply("ExpPump");

    public BlockExpPump() {
        super(Material.IRON, QuarryPlus.Names.exppump, ItemBlockPump::new);
        setHardness(5F);
        setDefaultState(getBlockState().getBaseState().withProperty(ACTING, false).withProperty(CONNECTED, false));
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
                                    EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!playerIn.isSneaking()) {
            if (!worldIn.isRemote) {
                TileEntity tileEntity = worldIn.getTileEntity(pos);
                if (tileEntity instanceof TileExpPump) {
                    TileExpPump expPump = (TileExpPump) tileEntity;
                    expPump.onActivated(worldIn, pos, playerIn);
                }
            }
            return true;
        }
        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        TileEntity entity = worldIn.getTileEntity(pos);
        if (entity != null) {
            IEnchantableTile.Util.init((IEnchantableTile) entity, stack.getEnchantmentTagList());
        }
    }

    @SuppressWarnings({"deprecation"})
    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn) {
        super.neighborChanged(state, worldIn, pos, blockIn);
        Optional.ofNullable((TileExpPump) worldIn.getTileEntity(pos)).ifPresent(TileExpPump::G_reinit);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, ACTING, CONNECTED);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        boolean work = state.getValue(ACTING);
        boolean connected = state.getValue(CONNECTED);
        return (work ? 1 : 0) | (connected ? 2 : 0);
    }

    @SuppressWarnings("deprecation")
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(ACTING, (meta & 1) == 1).withProperty(CONNECTED, (meta & 2) == 2);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileExpPump();
    }

    @Override
    protected boolean canRotate() {
        return false;
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
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        NonNullList<ItemStack> drops = NonNullList.create();
        getDrops(drops, world, pos, state, fortune);
        return drops;
    }

    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        TileEntity entity = world.getTileEntity(pos);
        if (entity instanceof TileExpPump) {
            TileExpPump pump = (TileExpPump) entity;
            ItemStack stack = new ItemStack(this);
            IEnchantableTile.Util.enchantmentToIS(pump, stack);
            drops.add(stack);
        }
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        if (!worldIn.isRemote) {
            TileEntity entity = worldIn.getTileEntity(pos);
            if (entity instanceof TileExpPump) {
                TileExpPump pump = (TileExpPump) entity;
                pump.onBreak(worldIn);
            }
        }
        super.breakBlock(worldIn, pos, state);
    }
}
