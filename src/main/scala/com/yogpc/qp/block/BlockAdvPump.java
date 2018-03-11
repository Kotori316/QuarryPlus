package com.yogpc.qp.block;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import com.yogpc.qp.Config;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.QuarryPlusI;
import com.yogpc.qp.compat.BuildcraftHelper;
import com.yogpc.qp.compat.InvUtils;
import com.yogpc.qp.item.ItemBlockAdvPump;
import com.yogpc.qp.tile.IEnchantableTile;
import com.yogpc.qp.tile.TileAdvPump;
import com.yogpc.qp.tile.TilePump;
import javax.annotation.Nullable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
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

public class BlockAdvPump extends ADismCBlock {
    public BlockAdvPump() {
        super(Material.ANVIL, QuarryPlus.Names.advpump, ItemBlockAdvPump::new);
        setHardness(1.5F);
        setResistance(10F);
        setSoundType(SoundType.STONE);
        setDefaultState(getBlockState().getBaseState().withProperty(ACTING, false));
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
                                    EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = playerIn.getHeldItem(hand);
        if (InvUtils.isDebugItem(playerIn, hand)) return true;
        if (BuildcraftHelper.isWrench(playerIn, hand, stack, new RayTraceResult(new Vec3d(hitX, hitY, hitZ), facing, pos))) {
            if (!worldIn.isRemote) {
                Optional.ofNullable((TileAdvPump) worldIn.getTileEntity(pos)).ifPresent(TileAdvPump::G_reinit);
            }
            return true;
        } else if (Config.content().debug() && stack.getItem() == Items.STICK) {
            Optional.ofNullable((TileAdvPump) worldIn.getTileEntity(pos)).ifPresent(TileAdvPump::toggleDelete);
            return true;
        } else if (stack.getItem() == QuarryPlusI.itemTool() && stack.getItemDamage() == 0) {
            if (!worldIn.isRemote)
                Optional.ofNullable((IEnchantableTile) worldIn.getTileEntity(pos)).ifPresent(t ->
                    t.sendEnchantMassage(playerIn));
            return true;
        } else if (!playerIn.isSneaking()) {
            playerIn.openGui(QuarryPlus.instance(), QuarryPlusI.guiIdAdvPump(), worldIn, pos.getX(), pos.getY(), pos.getZ());
            return true;
        }
        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
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

    //    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        TileEntity entity = world.getTileEntity(pos);
        if (TileAdvPump.class.isInstance(entity)) {
            TileAdvPump quarry = (TileAdvPump) entity;
            ItemStack stack = new ItemStack(QuarryPlusI.blockStandalonePump(), 1, 0);
            IEnchantableTile.Util.enchantmentToIS(quarry, stack);
            drops.add(stack);
        }
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        if (!worldIn.isRemote) {
            Consumer<TileAdvPump> consumer = pump -> IEnchantableTile.Util.init(pump, stack.getEnchantmentTagList());
            Optional.ofNullable((TileAdvPump) worldIn.getTileEntity(pos)).ifPresent(consumer.andThen(TilePump.requestTicket));
        }
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileAdvPump();
    }


    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, ACTING);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        boolean powered = state.getValue(ACTING);
        return (powered ? 8 : 0);
    }

    @SuppressWarnings("deprecation")
    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(ACTING, (meta & 8) == 8);
    }

    @Override
    protected boolean canRotate() {
        return false;
    }
}
