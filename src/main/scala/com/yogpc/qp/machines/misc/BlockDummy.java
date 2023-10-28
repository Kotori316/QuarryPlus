package com.yogpc.qp.machines.misc;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.Direction8;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractGlassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;

import java.util.ArrayList;
import java.util.HashSet;

public class BlockDummy extends AbstractGlassBlock {
    public static final String NAME = "dummy";
    public final BlockItem blockItem;

    public BlockDummy() {
        super(Properties.of(Material.GLASS)
            .noOcclusion()
            .noDrops()
            .isValidSpawn((state, world, pos, type) -> false)
            .isSuffocating((state, world, pos) -> false)
            .isRedstoneConductor((state, world, pos) -> false)
            .isViewBlocking((state, world, pos) -> false)
        );
        setRegistryName(QuarryPlus.modID, NAME);
        blockItem = new BlockItem(this, new Item.Properties().tab(Holder.TAB));
        blockItem.setRegistryName(QuarryPlus.modID, NAME);
    }

    private boolean breaking = false;

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            if (!breaking) {
                breaking = true;
                breakChain(world, pos);
                breaking = false;
            }
            super.onRemove(state, world, pos, newState, moved);
        }
    }

    private void breakChain(Level world, BlockPos first) {
        if (!world.isClientSide) {
            var chains = new HashSet<BlockPos>();
            chains.add(first);
            var nextCheck = new ArrayList<BlockPos>();
            nextCheck.add(first);
            rootLoop:
            while (!nextCheck.isEmpty()) {
                var copied = nextCheck.toArray(new BlockPos[0]);
                nextCheck.clear();
                for (var pos : copied) {
                    for (var dir : Direction8.DIRECTIONS) {
                        var nPos = pos.offset(dir.vec());
                        var nBlock = world.getBlockState(nPos);
                        if (nBlock.getBlock() == this) {
                            if (chains.add(nPos))
                                nextCheck.add(nPos);
                        }
                    }
                    if (chains.size() > Short.MAX_VALUE) {
                        break rootLoop;
                    }
                }
            }
            chains.forEach(pos -> world.removeBlock(pos, false));
        }
    }
}
