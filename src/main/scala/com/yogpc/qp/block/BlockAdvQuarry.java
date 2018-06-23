package com.yogpc.qp.block;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import com.yogpc.qp.Config;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.QuarryPlusI;
import com.yogpc.qp.compat.BuildcraftHelper;
import com.yogpc.qp.compat.InvUtils;
import com.yogpc.qp.item.ItemBlockEnchantable;
import com.yogpc.qp.tile.IEnchantableTile;
import com.yogpc.qp.tile.TileAdvQuarry;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
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
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockAdvQuarry extends ADismCBlock {

    public BlockAdvQuarry() {
        super(Material.IRON, QuarryPlus.Names.advquarry, ItemBlockEnchantable::new);
        setHardness(1.5F);
        setResistance(10F);
        setSoundType(SoundType.STONE);
        setDefaultState(getBlockState().getBaseState().withProperty(FACING, EnumFacing.NORTH).withProperty(ACTING, false));
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
        NonNullList<ItemStack> list = NonNullList.create();
        getDrops(list, world, pos, state, fortune);
        return list;
    }

    //    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        TileEntity entity = world.getTileEntity(pos);
        if (TileAdvQuarry.class.isInstance(entity)) {
            TileAdvQuarry quarry = (TileAdvQuarry) entity;
            ItemStack stack = new ItemStack(QuarryPlusI.blockChunkdestroyer(), 1, 0);
            IEnchantableTile.Util.enchantmentToIS(quarry, stack);
            drops.add(stack);
        }
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
                                    EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = playerIn.getHeldItem(hand);
        if (InvUtils.isDebugItem(playerIn, hand)) return true;
        if (BuildcraftHelper.isWrench(playerIn, hand, stack, new RayTraceResult(new Vec3d(hitX, hitY, hitZ), facing, pos))) {
            if (!worldIn.isRemote) {
                TileAdvQuarry quarry = (TileAdvQuarry) worldIn.getTileEntity(pos);
                if (quarry != null) {
                    quarry.G_reinit();
                    if (Config.content().noEnergy()) {
                        quarry.stickActivated(playerIn);
                    }
                }
            }
            return true;
        } else if (stack.getItem() == QuarryPlusI.itemTool()) {
            switch (stack.getItemDamage()) {
                case 0:
                    if (!worldIn.isRemote)
                        Optional.ofNullable((IEnchantableTile) worldIn.getTileEntity(pos)).ifPresent(t ->
                            t.sendEnchantMassage(playerIn));
                    break;
                case 2:
                    QuarryPlus.proxy.openAdvQuarryPumpGui(worldIn, playerIn, (TileAdvQuarry) worldIn.getTileEntity(pos), facing);
                    break;
            }

            return true;
        } else if (stack.getItem() == Items.STICK) {
            if (!worldIn.isRemote) {
                Optional.ofNullable((TileAdvQuarry) worldIn.getTileEntity(pos)).ifPresent(tileAdvQuarry -> {
                    if (Config.content().noEnergy())
                        tileAdvQuarry.stickActivated(playerIn);
                    tileAdvQuarry.startFillMode();
                });
            }
            return true;
        } else if (!playerIn.isSneaking()) {
            playerIn.openGui(QuarryPlus.instance(), QuarryPlusI.guiIdAdvQuarry(), worldIn, pos.getX(), pos.getY(), pos.getZ());
            return true;
        }
        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        if (!worldIn.isRemote) {
            EnumFacing facing = placer.getHorizontalFacing().getOpposite();
            worldIn.setBlockState(pos, state.withProperty(FACING, facing), 2);
            Consumer<TileAdvQuarry> consumer = quarry -> IEnchantableTile.Util.init(quarry, stack.getEnchantmentTagList());
            Optional.ofNullable((TileAdvQuarry) worldIn.getTileEntity(pos)).ifPresent(consumer.andThen(TileAdvQuarry.requestTicket));
        }
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
    @SuppressWarnings("deprecation")
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
        super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
        if (!worldIn.isRemote)
            Optional.ofNullable((TileAdvQuarry) worldIn.getTileEntity(pos)).ifPresent(TileAdvQuarry::energyConfigure);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileAdvQuarry();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(Item itemIn, CreativeTabs tab, NonNullList<ItemStack> list) {
        if (!Config.content().disableMapJ().get(TileAdvQuarry.SYMBOL())) {
            super.getSubBlocks(itemIn, tab, list);
        }
    }

    @Override
    protected boolean canRotate() {
        return true;
    }
}
