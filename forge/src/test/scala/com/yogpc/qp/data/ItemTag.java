package com.yogpc.qp.data;

import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

final class ItemTag extends TagsProvider<Item> {
    ItemTag(DataGenerator pGenerator, CompletableFuture<HolderLookup.Provider> provider, @Nullable ExistingFileHelper existingFileHelper) {
        super(pGenerator.getPackOutput(), Registries.ITEM, provider, QuarryPlus.modID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        var appender = tag(Holder.TAG_MARKERS);
        Stream.of(
                Holder.BLOCK_MARKER.blockItem,
                Holder.BLOCK_FLEX_MARKER.blockItem,
                Holder.BLOCK_16_MARKER.blockItem,
                Holder.BLOCK_WATERLOGGED_MARKER.blockItem,
                Holder.BLOCK_WATERLOGGED_FLEX_MARKER.blockItem,
                Holder.BLOCK_WATERLOGGED_16_MARKER.blockItem
            )
            .map(i -> ResourceKey.create(Registries.ITEM, i.getRegistryName()))
            .forEach(appender::add);
    }
}
