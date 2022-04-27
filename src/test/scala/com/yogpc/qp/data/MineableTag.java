package com.yogpc.qp.data;

import com.yogpc.qp.QuarryPlus;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

final class MineableTag extends TagsProvider<Block> {
    @SuppressWarnings("deprecation")
    MineableTag(DataGenerator pGenerator, @Nullable ExistingFileHelper existingFileHelper) {
        super(pGenerator, Registry.BLOCK, QuarryPlus.modID, existingFileHelper);
    }

    @Override
    protected void addTags() {
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
