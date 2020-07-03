package com.kotori316.marker;

import java.util.Collections;
import java.util.List;

import com.yogpc.qp.machines.base.IMarker;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

class ChunkMarkerInstance implements IMarker {
    private final Tile16Marker marker;

    ChunkMarkerInstance(Tile16Marker marker) {
        this.marker = marker;
    }

    @Override
    public boolean hasLink() {
        return true;
    }

    @Override
    public BlockPos min() {
        return marker.min() == BlockPos.ZERO ? marker.getPos() : marker.min();
    }

    @Override
    public BlockPos max() {
        return marker.max() == BlockPos.ZERO ? marker.getPos() : marker.max();
    }

    @Override
    public List<ItemStack> removeFromWorldWithItem() {
        if (marker.getWorld() != null) {
            List<ItemStack> drops = Block.getDrops(marker.getWorld().getBlockState(marker.getPos()), (ServerWorld) marker.getWorld(), marker.getPos(), marker);
            marker.getWorld().removeBlock(marker.getPos(), false);
            return drops;
        } else {
            return Collections.emptyList();
        }
    }

}
