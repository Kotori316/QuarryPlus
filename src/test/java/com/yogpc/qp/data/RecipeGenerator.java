package com.yogpc.qp.data;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.yogpc.qp.QuarryPlus;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.fabricmc.fabric.api.tag.convention.v1.ConventionalItemTags;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

final class RecipeGenerator extends FabricRecipeProvider {
    RecipeGenerator(FabricDataGenerator dataGenerator) {
        super(dataGenerator);
    }

    @Override
    protected void generateRecipes(Consumer<FinishedRecipe> exporter) {
        List<RecipeBuilder> builders = new ArrayList<>();
        builders.add(ShapedRecipeBuilder.shaped(QuarryPlus.ModObjects.BLOCK_QUARRY)
            .pattern("ioi")
            .pattern("gDg")
            .pattern("iRi")
            .define('R', Items.REDSTONE_BLOCK)
            .define('D', Items.DROPPER)
            .define('i', ConventionalItemTags.IRON_INGOTS)
            .define('g', Items.GOLDEN_PICKAXE)
            .define('o', Items.OBSIDIAN)
            .unlockedBy("has_obsidian", has(Items.OBSIDIAN))
        );
        /*builders.add(ShapedRecipeBuilder.shaped(QuarryPlus.ModObjects.BLOCK_MARKER)
            .pattern("glg")
            .pattern(" r ")
            .define('r', Items.REDSTONE_TORCH)
            .define('g', Items.GLOWSTONE_DUST)
            .define('l', ConventionalItemTags.LAPIS)
            .unlockedBy("has_redstone_torch", has(Items.REDSTONE_TORCH))
        );*/
        builders.add(ShapedRecipeBuilder.shaped(QuarryPlus.ModObjects.BLOCK_16_MARKER)
            .pattern("m ")
            .pattern("mm")
            .define('m', QuarryPlus.ModObjects.BLOCK_FLEX_MARKER)
            .unlockedBy("has_marker", has(QuarryPlus.ModObjects.BLOCK_FLEX_MARKER))
        );
        builders.add(ShapedRecipeBuilder.shaped(QuarryPlus.ModObjects.BLOCK_FLEX_MARKER)
            .pattern("glg")
            .pattern("lrl")
            .pattern("gl ")
            .define('r', Items.REDSTONE_TORCH)
            .define('g', Items.GLOWSTONE_DUST)
            .define('l', ConventionalItemTags.LAPIS)
            .unlockedBy("has_redstone_torch", has(Items.REDSTONE_TORCH))
        );
        builders.add(ShapedRecipeBuilder.shaped(QuarryPlus.ModObjects.BLOCK_ADV_PUMP)
            .pattern("bdb")
            .pattern("mqm")
            .pattern("gdg")
            .define('b', ConventionalItemTags.EMPTY_BUCKETS)
            .define('d', ConventionalItemTags.DIAMONDS)
            .define('g', ConventionalItemTags.GREEN_DYES)
            .define('q', QuarryPlus.ModObjects.BLOCK_QUARRY)
            .define('m', QuarryPlus.ModObjects.BLOCK_MARKER)
            .unlockedBy("has_quarry", has(QuarryPlus.ModObjects.BLOCK_QUARRY))
        );
        builders.add(ShapedRecipeBuilder.shaped(QuarryPlus.ModObjects.BLOCK_ADV_QUARRY)
            .pattern("mem")
            .pattern("gqg")
            .pattern("mem")
            .define('g', ConventionalItemTags.GREEN_DYES)
            .define('e', ConventionalItemTags.EMERALDS)
            .define('q', QuarryPlus.ModObjects.BLOCK_QUARRY)
            .define('m', QuarryPlus.ModObjects.BLOCK_16_MARKER)
            .unlockedBy("has_quarry", has(QuarryPlus.ModObjects.BLOCK_QUARRY))
        );
        builders.add(ShapedRecipeBuilder.shaped(QuarryPlus.ModObjects.BLOCK_PLACER)
            .pattern("GDG")
            .pattern("MRM")
            .pattern("MIM")
            .define('D', Items.DISPENSER)
            .define('R', Items.REDSTONE)
            .define('I', ConventionalItemTags.IRON_INGOTS)
            .define('M', Items.MOSSY_COBBLESTONE)
            .define('G', ConventionalItemTags.GOLD_INGOTS)
            .unlockedBy("has_dispenser", has(Items.DISPENSER))
        );
        builders.add(ShapedRecipeBuilder.shaped(QuarryPlus.ModObjects.BLOCK_REMOTE_PLACER)
            .pattern("eie")
            .pattern("dpd")
            .define('e', Items.ENDER_PEARL)
            .define('i', ConventionalItemTags.IRON_INGOTS)
            .define('d', ConventionalItemTags.DIAMONDS)
            .define('p', QuarryPlus.ModObjects.BLOCK_PLACER)
            .unlockedBy("has_placer", has(QuarryPlus.ModObjects.BLOCK_PLACER))
        );
        builders.add(ShapedRecipeBuilder.shaped(QuarryPlus.ModObjects.BLOCK_FILLER)
            .pattern("iii")
            .pattern("ala")
            .pattern("iii")
            .define('i', ConventionalItemTags.IRON_INGOTS)
            .define('a', Items.IRON_AXE)
            .define('l', Items.LADDER)
            .unlockedBy("has_iron_ingot", has(ConventionalItemTags.IRON_INGOTS))
        );
        builders.add(ShapedRecipeBuilder.shaped(QuarryPlus.ModObjects.ITEM_BEDROCK_MODULE)
            .pattern("ooo")
            .pattern(" m ")
            .pattern(" m ")
            .define('m', QuarryPlus.ModObjects.BLOCK_MARKER)
            .define('o', Items.OBSIDIAN)
            .unlockedBy("has_marker", has(QuarryPlus.ModObjects.BLOCK_MARKER))
        );
        builders.add(ShapedRecipeBuilder.shaped(QuarryPlus.ModObjects.ITEM_CHECKER)
            .pattern("ggg")
            .pattern("lrl")
            .define('r', QuarryPlus.ModObjects.BLOCK_MARKER)
            .define('g', ConventionalItemTags.GLASS_BLOCKS)
            .define('l', ConventionalItemTags.IRON_INGOTS)
            .unlockedBy("has_marker", has(QuarryPlus.ModObjects.BLOCK_MARKER))
        );
        builders.add(ShapedRecipeBuilder.shaped(QuarryPlus.ModObjects.ITEM_Y_SETTER)
            .pattern("ggg")
            .pattern("lrl")
            .define('r', QuarryPlus.ModObjects.BLOCK_MARKER)
            .define('g', ConventionalItemTags.GLASS_BLOCKS)
            .define('l', ConventionalItemTags.LAPIS)
            .unlockedBy("has_marker", has(QuarryPlus.ModObjects.BLOCK_MARKER))
        );
        builders.add(ShapedRecipeBuilder.shaped(QuarryPlus.ModObjects.BLOCK_WATERLOGGED_MARKER)
            .pattern("G")
            .pattern("M")
            .define('G', ConventionalItemTags.GREEN_DYES)
            .define('M', QuarryPlus.ModObjects.BLOCK_MARKER)
            .unlockedBy("has_marker", has(QuarryPlus.ModObjects.BLOCK_MARKER))
        );
        builders.add(ShapedRecipeBuilder.shaped(QuarryPlus.ModObjects.BLOCK_WATERLOGGED_FLEX_MARKER)
            .pattern("G")
            .pattern("M")
            .define('G', ConventionalItemTags.GREEN_DYES)
            .define('M', QuarryPlus.ModObjects.BLOCK_FLEX_MARKER)
            .unlockedBy("has_marker", has(QuarryPlus.ModObjects.BLOCK_FLEX_MARKER))
        );
        builders.add(ShapedRecipeBuilder.shaped(QuarryPlus.ModObjects.BLOCK_WATERLOGGED_16_MARKER)
            .pattern("G")
            .pattern("M")
            .define('G', ConventionalItemTags.GREEN_DYES)
            .define('M', QuarryPlus.ModObjects.BLOCK_16_MARKER)
            .unlockedBy("has_marker", has(QuarryPlus.ModObjects.BLOCK_16_MARKER))
        );

        builders.add(BedrockModuleRecipeBuilder.of(QuarryPlus.ModObjects.BLOCK_QUARRY)
            .saveName(new ResourceLocation(QuarryPlus.modID, "install_bedrock_module_quarry"))
            .unlockedBy("has_module", has(QuarryPlus.ModObjects.ITEM_BEDROCK_MODULE))
        );
        builders.add(BedrockModuleRecipeBuilder.of(QuarryPlus.ModObjects.BLOCK_ADV_QUARRY)
            .saveName(new ResourceLocation(QuarryPlus.modID, "install_bedrock_module_adv_quarry"))
            .unlockedBy("has_module", has(QuarryPlus.ModObjects.ITEM_BEDROCK_MODULE))
        );

        builders.forEach(b -> b.save(exporter));
    }
}
