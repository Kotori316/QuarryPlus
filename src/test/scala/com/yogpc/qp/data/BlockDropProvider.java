package com.yogpc.qp.data;

import java.util.Set;
import java.util.stream.Stream;

import com.yogpc.qp.Holder;
import com.yogpc.qp.machines.EnchantedLootFunction;
import com.yogpc.qp.machines.module.ModuleLootFunction;
import com.yogpc.qp.machines.quarry.QuarryLootFunction;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

class BlockDropProvider extends BlockLootSubProvider {

    protected BlockDropProvider() {
        super(Set.of(), FeatureFlags.DEFAULT_FLAGS);
    }

    @Override
    protected void generate() {
        Stream.of(
            Holder.BLOCK_CREATIVE_GENERATOR,
            Holder.BLOCK_CONTROLLER,
            Holder.BLOCK_MARKER,
            Holder.BLOCK_16_MARKER,
            Holder.BLOCK_FLEX_MARKER,
            Holder.BLOCK_WATERLOGGED_MARKER,
            Holder.BLOCK_WATERLOGGED_FLEX_MARKER,
            Holder.BLOCK_WATERLOGGED_16_MARKER,
            Holder.BLOCK_MINING_WELL,
            Holder.BLOCK_PUMP,
            Holder.BLOCK_WORKBENCH,
            Holder.BLOCK_MOVER,
            Holder.BLOCK_EXP_PUMP,
            Holder.BLOCK_PLACER,
            Holder.BLOCK_REMOTE_PLACER,
            Holder.BLOCK_REPLACER,
            Holder.BLOCK_BOOK_MOVER,
            Holder.BLOCK_SOLID_FUEL_QUARRY,
            Holder.BLOCK_FILLER
        ).forEach(this::dropSelf);
        Stream.of(
            Holder.BLOCK_MINI_QUARRY,
            Holder.BLOCK_ADV_PUMP
        ).forEach(b ->
            add(b, createSingleItemTable(b).apply(EnchantedLootFunction.builder()))
        );
        Stream.of(
            Holder.BLOCK_QUARRY
        ).forEach(b ->
            add(b, createSingleItemTable(b).apply(QuarryLootFunction.builder()).apply(ModuleLootFunction.builder()))
        );
        Stream.of(
            Holder.BLOCK_ADV_QUARRY
        ).forEach(b ->
            add(b, createSingleItemTable(b).apply(ModuleLootFunction.builder()))
        );
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return Holder.blocks().stream().map(Holder.NamedEntry::t).map(Block.class::cast).toList();
    }
}
