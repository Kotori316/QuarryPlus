package com.yogpc.qp.machines.base;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

/**
 * Must be implemented by subclass of {@link TileEntity}.
 */
public interface IMarker {

    /**
     * If this marker has made link.
     */
    boolean hasLink();

    BlockPos min();

    BlockPos max();

    /**
     * Called to remove this marker from the world.
     * In this method, you must destroy this block by calling World#setBlockToAir(BlockPos) and so on.
     *
     * @return a list of drop items.
     */
    List<ItemStack> removeFromWorldWithItem();

}
