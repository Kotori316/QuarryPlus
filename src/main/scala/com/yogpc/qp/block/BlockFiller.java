package com.yogpc.qp.block;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.QuarryPlusI;
import com.yogpc.qp.tile.TileFiller;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockFiller extends ADismCBlock {
    public BlockFiller() {
        super(Material.IRON, QuarryPlus.Names.filler, ItemBlock::new);
        setHardness(1.5F);
        setResistance(10F);
        setSoundType(SoundType.STONE);
    }

    @Override
    protected boolean canRotate() {
        return false;
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileFiller();
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
                                    EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ)) return true;

        if (!playerIn.isSneaking()) {
            playerIn.openGui(QuarryPlus.instance(), QuarryPlusI.guiIdFiller(), worldIn, pos.getX(), pos.getY(), pos.getZ());
            return true;
        }
        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        if (!worldIn.isRemote) {
            TileEntity entity = worldIn.getTileEntity(pos);
            if (entity instanceof TileFiller) {
                TileFiller tileFiller = (TileFiller) entity;
                InventoryHelper.dropInventoryItems(worldIn, pos, tileFiller.moduleInv());
                InventoryHelper.dropInventoryItems(worldIn, pos, tileFiller.inventory());
                worldIn.updateComparatorOutputLevel(pos, state.getBlock());
            }
        }
        super.breakBlock(worldIn, pos, state);
    }
}
