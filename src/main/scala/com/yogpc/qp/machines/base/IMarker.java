package com.yogpc.qp.machines.base;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

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

    final class Cap implements Capability.IStorage<IMarker>, Callable<IMarker> {
        @CapabilityInject(IMarker.class)
        public static final Capability<IMarker> MARKER_CAP = null;

        @Override
        public IMarker call() {
            return EMPTY_MARKER;
        }

        @Override
        public INBT writeNBT(Capability<IMarker> capability, IMarker instance, Direction side) {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putLong("min", instance.min().toLong());
            nbt.putLong("max", instance.max().toLong());
            return nbt;
        }

        @Override
        public void readNBT(Capability<IMarker> capability, IMarker instance, Direction side, INBT nbt) {
            // Default marker instance is immutable.
        }

        public static Capability<IMarker> MARKER_CAPABILITY() {
            return MARKER_CAP;
        }

        public static void register() {
            Cap cap = new Cap();
            CapabilityManager.INSTANCE.register(IMarker.class, cap, cap);
        }
    }

    class MarkerImpl implements IMarker {
        private final BlockPos min;
        private final BlockPos max;

        public MarkerImpl(BlockPos min, BlockPos max) {
            this.min = min;
            this.max = max;
        }

        @Override
        public boolean hasLink() {
            return true;
        }

        @Override
        public BlockPos min() {
            return min;
        }

        @Override
        public BlockPos max() {
            return max;
        }

        @Override
        public List<ItemStack> removeFromWorldWithItem() {
            return Collections.emptyList();
        }
    }

    IMarker EMPTY_MARKER = new MarkerImpl(BlockPos.ZERO, BlockPos.ZERO);
}
