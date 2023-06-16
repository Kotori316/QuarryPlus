package com.yogpc.qp.machines.misc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.yogpc.qp.machines.Direction8;
import com.yogpc.qp.machines.QPBlock;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractGlassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

public class BlockDummy extends AbstractGlassBlock {
    public static final String NAME = "dummy";
    public final BlockItem blockItem;

    public BlockDummy() {
        super(FabricBlockSettings.create()
            .nonOpaque()
            .drops(BuiltInLootTables.EMPTY)
            .allowsSpawning((state, world, pos, type) -> false)
            .solidBlock((state, world, pos) -> false)
            .suffocates((state, world, pos) -> false)
            .blockVision((state, world, pos) -> false)
        );
        blockItem = new QPBlock.QPBlockItem(this, new FabricItemSettings());
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
            while (!nextCheck.isEmpty()) {
                var list = List.copyOf(nextCheck);
                nextCheck.clear();
                for (BlockPos pos : list) {
                    for (var dir : Direction8.DIRECTIONS) {
                        var nPos = pos.offset(dir.vec());
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
