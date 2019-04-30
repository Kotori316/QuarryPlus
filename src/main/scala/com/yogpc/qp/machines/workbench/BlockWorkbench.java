package com.yogpc.qp.machines.workbench;

import java.util.Optional;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.base.QPBlock;
import com.yogpc.qp.utils.Holder;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import static jp.t2v.lab.syntax.MapStreamSyntax.optCast;

public class BlockWorkbench extends QPBlock {
    public BlockWorkbench() {
        super(Properties.create(Material.ANVIL).hardnessAndResistance(3.0f), QuarryPlus.Names.workbench, ItemBlock::new);
    }

    @Override
    public boolean onBlockActivated(IBlockState state, World worldIn, BlockPos pos, EntityPlayer player,
                                    EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (super.onBlockActivated(state, worldIn, pos, player, hand, side, hitX, hitY, hitZ)) {
            return true;
        }
        if (!player.isSneaking()) {
            if(!worldIn.isRemote){
                Optional.ofNullable(worldIn.getTileEntity(pos))
                    .flatMap(optCast(TileWorkbench.class))
                    .ifPresent(t ->
                        NetworkHooks.openGui(((EntityPlayerMP) player), t, pos));
            }
            return true;
        }
        return false;
    }

    @Override
    public void onReplaced(IBlockState state, World worldIn, BlockPos pos, IBlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            if (!worldIn.isRemote) {
                TileEntity entity = worldIn.getTileEntity(pos);
                if (entity instanceof TileWorkbench) {
                    TileWorkbench inventory = (TileWorkbench) entity;
                    for (ItemStack itemstack : inventory.inventory) {
                        InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), itemstack);
                    }
                    worldIn.updateComparatorOutputLevel(pos, state.getBlock());
                }
            }
        }
        super.onReplaced(state, worldIn, pos, newState, isMoving);
    }

    @Override
    public TileEntity createNewTileEntity(IBlockReader worldIn) {
        return Holder.workbenchTileType().create();
    }
}
