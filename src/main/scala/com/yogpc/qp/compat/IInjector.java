package com.yogpc.qp.compat;

import java.util.stream.Stream;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

interface IInjector {

    Stream<? extends IInjector> getInjector(ItemStack stack, TileEntity entity, EnumFacing facing);

    ItemStack inject(ItemStack stack, World world, BlockPos fromPos);
}
