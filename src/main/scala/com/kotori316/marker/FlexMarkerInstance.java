package com.kotori316.marker;

import java.util.List;
import java.util.Objects;

import com.yogpc.qp.machines.base.IMarker;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

class FlexMarkerInstance implements IMarker {
    private final TileFlexMarker marker;

    FlexMarkerInstance(TileFlexMarker marker) {
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
        Objects.requireNonNull(marker.getWorld());
        List<ItemStack> list = Block.getDrops(marker.getWorld().getBlockState(marker.getPos()), ((ServerWorld) marker.getWorld()), marker.getPos(), marker);
        marker.getWorld().removeBlock(marker.getPos(), false);
        return list;
    }

}
