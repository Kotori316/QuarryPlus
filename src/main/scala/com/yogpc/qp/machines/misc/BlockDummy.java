package com.yogpc.qp.machines.misc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.Direction8;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.TransparentBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class BlockDummy extends TransparentBlock {
    public static final String NAME = "dummy";
    public final BlockItem blockItem;

    public BlockDummy() {
        super(FabricBlockSettings.of(Material.GLASS)
            .nonOpaque()
            .dropsNothing()
            .allowsSpawning((state, world, pos, type) -> false)
            .solidBlock((state, world, pos) -> false)
            .suffocates((state, world, pos) -> false)
            .blockVision((state, world, pos) -> false)
            .luminance(15)
        );
        blockItem = new BlockItem(this, new FabricItemSettings().group(QuarryPlus.CREATIVE_TAB));
    }

    private boolean breaking = false;

    @Override
    public boolean isTranslucent(BlockState state, BlockView world, BlockPos pos) {
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            boolean firstBreak;
            if (breaking) {
                firstBreak = false;
            } else {
                firstBreak = true;
                breaking = true;
            }
            if (firstBreak) {
                breakChain(world, pos);
                breaking = false;
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    private void breakChain(World world, BlockPos first) {
        if (!world.isClient) {
            var chains = new HashSet<BlockPos>();
            chains.add(first);
            var nextCheck = new ArrayList<BlockPos>();
            nextCheck.add(first);
            while (!nextCheck.isEmpty()) {
                var list = List.copyOf(nextCheck);
                nextCheck.clear();
                for (var pos : list) {
                    for (var dir : Direction8.DIRECTIONS) {
                        var nPos = pos.add(dir.vec());
                        var nBlock = world.getBlockState(nPos);
                        if (nBlock.getBlock() == this) {
                            if (chains.add(nPos))
                                nextCheck.add(nPos);
                        }
                    }
                }
            }
            chains.forEach(pos -> world.removeBlock(pos, false));
        }
    }
}
