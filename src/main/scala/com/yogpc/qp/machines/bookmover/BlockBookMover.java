package com.yogpc.qp.machines.bookmover;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.compat.InvUtils;
import com.yogpc.qp.machines.base.QPBlock;
import com.yogpc.qp.utils.Holder;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import scala.Symbol;

public class BlockBookMover extends QPBlock {
    public static final Symbol SYMBOL = Symbol.apply("EnchantMoverFromBook");
    public static final String GUI_ID = QuarryPlus.modID + ":gui_" + QuarryPlus.Names.moverfrombook;

    public BlockBookMover() {
        super(Properties.create(Material.IRON).hardnessAndResistance(1.2f), QuarryPlus.Names.moverfrombook, BlockItem::new);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player,
                                    Hand hand, BlockRayTraceResult hit) {
        if (InvUtils.isDebugItem(player, hand)) return false;
        TileBookMover mover = ((TileBookMover) worldIn.getTileEntity(pos));
        if (!player.isSneaking() && mover != null && mover.enabled()) {
            if (!worldIn.isRemote) {
                NetworkHooks.openGui(((ServerPlayerEntity) player), mover, pos);
            }
            return true;
        }
        return false;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            InvUtils.dropAndUpdateInv(worldIn, pos, (TileBookMover) worldIn.getTileEntity(pos), this);
            super.onReplaced(state, worldIn, pos, newState, isMoving);
        }
    }

    @Override
    public TileEntity createNewTileEntity(IBlockReader worldIn) {
        return Holder.bookMoverType().create();
    }
}
