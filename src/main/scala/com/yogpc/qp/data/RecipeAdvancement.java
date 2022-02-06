package com.yogpc.qp.data;

import java.util.List;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.module.ExpModuleItem;
import com.yogpc.qp.machines.module.FillerModuleItem;
import com.yogpc.qp.machines.module.PumpModuleItem;
import com.yogpc.qp.machines.module.ReplacerModuleItem;
import com.yogpc.qp.machines.placer.PlacerBlock;
import com.yogpc.qp.machines.quarry.SFQuarryBlock;
import com.yogpc.qp.machines.workbench.BlockWorkbench;
import com.yogpc.qp.machines.workbench.EnableCondition;
import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.crafting.conditions.NotCondition;

final class RecipeAdvancement extends QuarryPlusDataProvider.QuarryDataProvider {
    RecipeAdvancement(DataGenerator generatorIn) {
        super(generatorIn);
    }

    @Override
    String directory() {
        return "advancements/recipes";
    }

    @Override
    List<? extends QuarryPlusDataProvider.DataBuilder> data() {
        return List.of(
            // Workbench
            AdvancementSerializeHelper.apply(Holder.BLOCK_WORKBENCH.getRegistryName())
                .addTagCriterion(Tags.Items.STORAGE_BLOCKS_IRON)
                .addTagCriterion(Tags.Items.STORAGE_BLOCKS_GOLD)
                .addItemCriterion(Items.REDSTONE)
                .addCondition(new EnableCondition(BlockWorkbench.NAME)),
            // Flexible Marker
            AdvancementSerializeHelper.apply(Holder.BLOCK_FLEX_MARKER.getRegistryName())
                .addItemCriterion(Holder.BLOCK_MARKER),
            // Chunk Marker
            AdvancementSerializeHelper.apply(id("chunk_marker"), Holder.BLOCK_16_MARKER.getRegistryName())
                .addItemCriterion(Holder.BLOCK_MARKER),
            // Pump Module
            AdvancementSerializeHelper.apply(Holder.ITEM_PUMP_MODULE.getRegistryName())
                .addItemCriterion(Holder.BLOCK_PUMP)
                .addCondition(new EnableCondition(PumpModuleItem.NAME)),
            // Exp Module
            AdvancementSerializeHelper.apply(Holder.ITEM_EXP_MODULE.getRegistryName())
                .addItemCriterion(Holder.BLOCK_EXP_PUMP)
                .addCondition(new EnableCondition(ExpModuleItem.NAME)),
            // Replacer Module
            AdvancementSerializeHelper.apply(Holder.ITEM_REPLACER_MODULE.getRegistryName())
                .addItemCriterion(Holder.BLOCK_REPLACER)
                .addCondition(new EnableCondition(ReplacerModuleItem.NAME)),
            // Placer
            AdvancementSerializeHelper.apply(id("placer_plus_crafting"), id("placer_plus_crafting"))
                .addItemCriterion(Items.DISPENSER)
                .addItemCriterion(Items.MOSSY_COBBLESTONE)
                .addTagCriterion(Tags.Items.INGOTS_GOLD)
                .addCondition(new EnableCondition(PlacerBlock.NAME))
                .addCondition(new NotCondition(new EnableCondition(BlockWorkbench.NAME))),
            // Solid Fuel Quarry
            AdvancementSerializeHelper.apply(Holder.BLOCK_SOLID_FUEL_QUARRY.getRegistryName())
                .addItemCriterion(Items.DIAMOND_PICKAXE)
                .addTagCriterion(Tags.Items.STORAGE_BLOCKS_GOLD)
                .addCondition(new EnableCondition(SFQuarryBlock.NAME)),
            // Filler Module
            AdvancementSerializeHelper.apply(Holder.ITEM_FILLER_MODULE.getRegistryName())
                .addItemCriterion(Holder.BLOCK_FILLER)
                .addCondition(new EnableCondition(FillerModuleItem.NAME))
        );
    }

    private static ResourceLocation id(String name) {
        return new ResourceLocation(QuarryPlus.modID, name);
    }

}
