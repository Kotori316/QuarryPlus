package com.yogpc.qp.tile;

import java.util.Collections;
import java.util.List;

import buildcraft.api.core.IAreaProvider;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
     * In this method, you must destroy this block by calling {@link net.minecraft.world.World#setBlockToAir(BlockPos)} and so on.
     *
     * @return a list of drop items.
     */
    List<ItemStack> removeFromWorldWithItem();

    default void removeAndDrop(World world, BlockPos pos) {
        removeFromWorldWithItem().forEach(i ->
            InventoryHelper.spawnItemStack(world, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, i));
    }

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
