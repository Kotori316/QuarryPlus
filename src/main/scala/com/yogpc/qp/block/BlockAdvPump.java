package com.yogpc.qp.block;

import java.util.Optional;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.QuarryPlusI;
import com.yogpc.qp.compat.EnchantmentHelper;
import com.yogpc.qp.item.ItemBlockEnchantable;
import com.yogpc.qp.tile.TileAdvPump;
import javax.annotation.Nullable;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockAdvPump extends ADismCBlock {
    public BlockAdvPump() {
        super(Material.IRON, QuarryPlus.Names.advpump, ItemBlockEnchantable::new);
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
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        TileEntity entity = world.getTileEntity(pos);
        if (TileAdvPump.class.isInstance(entity)) {
            TileAdvPump quarry = (TileAdvPump) entity;
            ItemStack stack = new ItemStack(QuarryPlusI.blockStandalonePump, 1, 0);
            EnchantmentHelper.enchantmentToIS(quarry, stack);
            drops.add(stack);
        }
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
        if (!worldIn.isRemote) {
            Optional.ofNullable((TileAdvPump) worldIn.getTileEntity(pos)).ifPresent(pump -> {
                pump.requestTicket();
                EnchantmentHelper.init(pump, stack.getEnchantmentTagList());
            });
        }
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileAdvPump();
    }
}
