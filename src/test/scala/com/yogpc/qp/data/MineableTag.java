package com.yogpc.qp.data;

import com.yogpc.qp.QuarryPlus;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

final class MineableTag extends TagsProvider<Block> {
    MineableTag(DataGenerator pGenerator, CompletableFuture<HolderLookup.Provider> provider, @Nullable ExistingFileHelper existingFileHelper) {
        super(pGenerator.getPackOutput(), Registries.BLOCK, provider, QuarryPlus.modID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        /*this.tag(BlockTags.MINEABLE_WITH_PICKAXE).add(
            Holder.BLOCK_QUARRY,
            Holder.BLOCK_ADV_QUARRY,
            Holder.BLOCK_PUMP,
            Holder.BLOCK_EXP_PUMP,
            Holder.BLOCK_ADV_PUMP,
            Holder.BLOCK_MINING_WELL,
            Holder.BLOCK_PLACER,
            Holder.BLOCK_REMOTE_PLACER,
            Holder.BLOCK_FILLER,
            Holder.BLOCK_CREATIVE_GENERATOR
        );*/
    }

    @Override
    public String getName() {
        return getClass().getName();
    }
}
