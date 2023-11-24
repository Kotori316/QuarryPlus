package com.yogpc.qp.data;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import com.yogpc.qp.QuarryPlus;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

final class TagGenerator extends TagsProvider<Item> {
    TagGenerator(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> completableFuture) {
        super(output, Registries.ITEM, completableFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        var appender = tag(QuarryPlus.ModObjects.TAG_MARKERS);
        Stream.of(
                QuarryPlus.ModObjects.BLOCK_MARKER.blockItem,
                QuarryPlus.ModObjects.BLOCK_FLEX_MARKER.blockItem,
                QuarryPlus.ModObjects.BLOCK_16_MARKER.blockItem,
                QuarryPlus.ModObjects.BLOCK_WATERLOGGED_MARKER.blockItem,
                QuarryPlus.ModObjects.BLOCK_WATERLOGGED_FLEX_MARKER.blockItem,
                QuarryPlus.ModObjects.BLOCK_WATERLOGGED_16_MARKER.blockItem
            )
            .map(i -> ResourceKey.create(Registries.ITEM, i.getRegistryName()))
            .forEach(appender::add);
    }
}
