package com.yogpc.qp.tile;

import java.util.Collections;
import java.util.List;

import buildcraft.api.core.IAreaProvider;
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

    class BCWrapper implements IMarker {

        private final IAreaProvider provider;

        public BCWrapper(IAreaProvider provider) {
            this.provider = provider;
        }

        @Override
        public boolean hasLink() {
            BlockPos min = min();
            BlockPos max = max();
            return min.getX() != max.getX() && min.getZ() != max.getZ();
        }

        @Override
        public BlockPos min() {
            return provider.min();
        }

        @Override
        public BlockPos max() {
            return provider.max();
        }

        @Override
        public List<ItemStack> removeFromWorldWithItem() {
            provider.removeFromWorld();
            return Collections.emptyList();
        }
    }
}
