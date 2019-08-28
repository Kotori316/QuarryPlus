package com.yogpc.qp.compat;

import java.util.stream.Stream;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

interface IInjector {

    Stream<? extends IInjector> getInjector(ItemStack stack, TileEntity entity, Direction facing);

    ItemStack inject(ItemStack stack, World world, BlockPos fromPos);
}
