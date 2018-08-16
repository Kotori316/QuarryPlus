package com.yogpc.qp.block;

import java.util.Optional;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.item.ItemBlockPump;
import com.yogpc.qp.tile.IEnchantableTile;
import com.yogpc.qp.tile.TileExpPump;
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
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
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

}
