package com.yogpc.qp.data;

import com.yogpc.qp.QuarryPlus;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.core.Registry;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.item.Item;

final class TagGenerator extends TagsProvider<Item> {
    TagGenerator(FabricDataGenerator dataGenerator) {
        super(dataGenerator, Registry.ITEM);
    }

    @Override
    protected void addTags() {
        tag(QuarryPlus.ModObjects.TAG_MARKERS)
            .add(QuarryPlus.ModObjects.BLOCK_MARKER.blockItem)
            .add(QuarryPlus.ModObjects.BLOCK_FLEX_MARKER.blockItem)
            .add(QuarryPlus.ModObjects.BLOCK_16_MARKER.blockItem)
            .add(QuarryPlus.ModObjects.BLOCK_WATERLOGGED_MARKER.blockItem)
            .add(QuarryPlus.ModObjects.BLOCK_WATERLOGGED_FLEX_MARKER.blockItem)
            .add(QuarryPlus.ModObjects.BLOCK_WATERLOGGED_16_MARKER.blockItem);
    }
}
