package com.yogpc.qp.data;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

final class ItemTag extends TagsProvider<Item> {
    @SuppressWarnings("deprecation")
    ItemTag(DataGenerator pGenerator, @Nullable ExistingFileHelper existingFileHelper) {
        super(pGenerator, Registry.ITEM, QuarryPlus.modID, existingFileHelper);
    }

    @Override
    protected void addTags() {
        tag(Holder.TAG_MARKERS)
            .add(Holder.BLOCK_MARKER.blockItem)
            .add(Holder.BLOCK_FLEX_MARKER.blockItem)
            .add(Holder.BLOCK_16_MARKER.blockItem)
            .add(Holder.BLOCK_WATERLOGGED_MARKER.blockItem)
            .add(Holder.BLOCK_WATERLOGGED_FLEX_MARKER.blockItem)
            .add(Holder.BLOCK_WATERLOGGED_16_MARKER.blockItem);
    }
}
