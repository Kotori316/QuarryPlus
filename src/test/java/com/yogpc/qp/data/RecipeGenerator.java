package com.yogpc.qp.data;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.yogpc.qp.QuarryPlus;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
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
            .define('i', Items.IRON_INGOT)
            .define('g', Items.GOLDEN_PICKAXE)
            .define('o', Items.OBSIDIAN)
            .unlockedBy("has_obsidian", has(Items.OBSIDIAN))
        );
        builders.add(ShapedRecipeBuilder.shaped(QuarryPlus.ModObjects.BLOCK_MARKER)
            .pattern("glg")
            .pattern(" r ")
            .define('r', Items.REDSTONE_TORCH)
            .define('g', Items.GLOWSTONE_DUST)
            .define('l', Items.LAPIS_LAZULI)
            .unlockedBy("has_redstone_torch", has(Items.REDSTONE_TORCH))
        );
        builders.add(ShapedRecipeBuilder.shaped(QuarryPlus.ModObjects.BLOCK_16_MARKER)
            .pattern("m ")
            .pattern("mm")
            .define('m', QuarryPlus.ModObjects.BLOCK_MARKER)
            .unlockedBy("has_marker", has(QuarryPlus.ModObjects.BLOCK_MARKER))
        );
        builders.add(ShapedRecipeBuilder.shaped(QuarryPlus.ModObjects.BLOCK_FLEX_MARKER)
            .pattern(" m ")
            .pattern("mlm")
            .pattern(" m ")
            .define('m', QuarryPlus.ModObjects.BLOCK_MARKER)
            .define('l', Items.LAPIS_LAZULI)
            .unlockedBy("has_marker", has(QuarryPlus.ModObjects.BLOCK_MARKER))
        );
        builders.add(ShapedRecipeBuilder.shaped(QuarryPlus.ModObjects.BLOCK_ADV_PUMP)
            .pattern("bdb")
            .pattern("mqm")
            .pattern("gdg")
            .define('b', Items.BUCKET)
            .define('d', Items.DIAMOND)
            .define('g', Items.GREEN_DYE)
            .define('q', QuarryPlus.ModObjects.BLOCK_QUARRY)
            .define('m', QuarryPlus.ModObjects.BLOCK_MARKER)
            .unlockedBy("has_quarry", has(QuarryPlus.ModObjects.BLOCK_QUARRY))
        );
        builders.add(ShapedRecipeBuilder.shaped(QuarryPlus.ModObjects.BLOCK_ADV_QUARRY)
            .pattern("mem")
            .pattern("gqg")
            .pattern("mem")
            .define('g', Items.GREEN_DYE)
            .define('e', Items.EMERALD)
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
            .define('I', Items.IRON_INGOT)
            .define('M', Items.MOSSY_COBBLESTONE)
            .define('G', Items.GOLD_INGOT)
            .unlockedBy("has_dispenser", has(Items.DISPENSER))
        );
        builders.add(ShapedRecipeBuilder.shaped(QuarryPlus.ModObjects.BLOCK_FILLER)
            .pattern("iii")
            .pattern("ala")
            .pattern("iii")
            .define('i', Items.IRON_INGOT)
            .define('a', Items.IRON_AXE)
            .define('l', Items.LADDER)
            .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
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
            .define('g', Items.GLASS)
            .define('l', Items.IRON_INGOT)
            .unlockedBy("has_marker", has(QuarryPlus.ModObjects.BLOCK_MARKER))
        );
        builders.add(ShapedRecipeBuilder.shaped(QuarryPlus.ModObjects.ITEM_Y_SETTER)
            .pattern("ggg")
            .pattern("lrl")
            .define('r', QuarryPlus.ModObjects.BLOCK_MARKER)
            .define('g', Items.GLASS)
            .define('l', Items.LAPIS_LAZULI)
            .unlockedBy("has_marker", has(QuarryPlus.ModObjects.BLOCK_MARKER))
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
