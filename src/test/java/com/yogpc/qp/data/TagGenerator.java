package com.yogpc.qp.data;

import com.yogpc.qp.QuarryPlus;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.Registry;
import net.minecraft.world.item.Item;

final class TagGenerator extends FabricTagProvider<Item> {
    TagGenerator(FabricDataGenerator dataGenerator) {
        super(dataGenerator, Registry.ITEM);
    }

    @Override
    protected void generateTags() {
        tag(QuarryPlus.ModObjects.TAG_MARKERS)
            .add(QuarryPlus.ModObjects.BLOCK_MARKER.blockItem)
            .add(QuarryPlus.ModObjects.BLOCK_FLEX_MARKER.blockItem)
            .add(QuarryPlus.ModObjects.BLOCK_16_MARKER.blockItem)
            .add(QuarryPlus.ModObjects.BLOCK_WATERLOGGED_MARKER.blockItem)
            .add(QuarryPlus.ModObjects.BLOCK_WATERLOGGED_FLEX_MARKER.blockItem)
            .add(QuarryPlus.ModObjects.BLOCK_WATERLOGGED_16_MARKER.blockItem);
    }
}
