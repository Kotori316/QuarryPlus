package com.yogpc.qp.block;

import com.yogpc.qp.Config;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.QuarryPlusI;
import com.yogpc.qp.compat.InvUtils;
import com.yogpc.qp.tile.TileBookMover;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import scala.Symbol;

public class BlockBookMover extends ADismCBlock {
    public static final Symbol SYMBOL = Symbol.apply("EnchantMoverFromBook");

    public BlockBookMover() {
        super(Material.IRON, QuarryPlus.Names.moverfrombook, ItemBlock::new);
        setHardness(1.2F);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn,
                                    EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (InvUtils.isDebugItem(playerIn, hand)) return true;
        if (!playerIn.isSneaking() && !Config.content().disableMapJ().get(SYMBOL)) {
            playerIn.openGui(QuarryPlus.instance(), QuarryPlusI.guiIdMoverFromBook(), worldIn, pos.getX(), pos.getY(), pos.getZ());
            return true;
        }
        return super.onBlockActivated(worldIn, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    protected boolean canRotate() {
        return false;
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileBookMover();
    }
}
