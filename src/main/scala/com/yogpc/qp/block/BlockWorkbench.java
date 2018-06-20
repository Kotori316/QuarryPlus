package com.yogpc.qp.block;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.QuarryPlusI;
import com.yogpc.qp.compat.InvUtils;
import com.yogpc.qp.tile.TileWorkbench;
import com.yogpc.qp.version.VersionUtil;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockWorkbench extends ADismCBlock {

    public BlockWorkbench() {
        super(Material.IRON, QuarryPlus.Names.workbench, ItemBlock::new);
        setHardness(3F);
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        TileEntity entity = worldIn.getTileEntity(pos);
        if (TileWorkbench.class.isInstance(entity)) {
            TileWorkbench inventory = (TileWorkbench) entity;
            for (int i = 0; i < 27; ++i) {
                ItemStack itemstack = inventory.getStackInSlot(i);

                if (VersionUtil.nonEmpty(itemstack)) {
                    float f = RANDOM.nextFloat() * 0.8F + 0.1F;
                    float f1 = RANDOM.nextFloat() * 0.8F + 0.1F;
                    float f2 = RANDOM.nextFloat() * 0.8F + 0.1F;

                    while (VersionUtil.nonEmpty(itemstack)) {
                        EntityItem entityitem = new EntityItem(worldIn, pos.getX() + (double) f, pos.getY() + (double) f1, pos.getZ() + (double) f2,
                            itemstack.splitStack(Math.min(RANDOM.nextInt(21) + 10, itemstack.getMaxStackSize())));
                        entityitem.motionX = RANDOM.nextGaussian() * 0.05D;
                        entityitem.motionY = RANDOM.nextGaussian() * 0.05D + 0.2D;
                        entityitem.motionZ = RANDOM.nextGaussian() * 0.05D;
                        worldIn.spawnEntity(entityitem);
                    }
                }
            }
            worldIn.updateComparatorOutputLevel(pos, state.getBlock());
        }
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public TileWorkbench createNewTileEntity(World worldIn, int meta) {
        return new TileWorkbench();
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
                                    EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (InvUtils.isDebugItem(playerIn, hand)) return true;
        if (!playerIn.isSneaking()) {
            playerIn.openGui(QuarryPlus.INSTANCE, QuarryPlusI.guiIdWorkbench(), worldIn, pos.getX(), pos.getY(), pos.getZ());
            return true;
        }
        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    protected boolean canRotate() {
        return false;
    }
}
