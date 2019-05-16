package com.yogpc.qp.machines.bookmover;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.compat.InvUtils;
import com.yogpc.qp.machines.base.QPBlock;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import scala.Symbol;

public class BlockBookMover extends QPBlock {
    public static final Symbol SYMBOL = Symbol.apply("EnchantMoverFromBook");
    public static final String GUI_ID = QuarryPlus.modID + ":gui_" + QuarryPlus.Names.moverfrombook;

    public BlockBookMover() {
        super(Properties.create(Material.IRON).hardnessAndResistance(1.2f), QuarryPlus.Names.moverfrombook, ItemBlock::new);
    }

    @Override
    public boolean onBlockActivated(IBlockState state, World worldIn, BlockPos pos, EntityPlayer player,
                                    EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (super.onBlockActivated(state, worldIn, pos, player, hand, side, hitX, hitY, hitZ)) return true;
        TileBookMover mover = ((TileBookMover) worldIn.getTileEntity(pos));
        if (!player.isSneaking() && mover != null && mover.enabled()) {
            if (!worldIn.isRemote) {
                NetworkHooks.openGui(((EntityPlayerMP) player), mover, pos);
            }
            return true;
        }
        return false;
    }

    @Override
    public void onReplaced(IBlockState state, World worldIn, BlockPos pos, IBlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            InvUtils.dropAndUpdateInv(worldIn, pos, (TileBookMover) worldIn.getTileEntity(pos), this);
            super.onReplaced(state, worldIn, pos, newState, isMoving);
        }
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(IBlockReader worldIn) {
        return null;
    }
}
