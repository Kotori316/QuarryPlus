package com.yogpc.qp.data

import com.yogpc.qp.machine.exp.ExpModuleItem
import com.yogpc.qp.machine.marker.{ChunkMarkerBlock, FlexibleMarkerBlock}
import com.yogpc.qp.machine.module.RepeatTickModuleItem
import com.yogpc.qp.recipe.InstallBedrockModuleRecipe
import com.yogpc.qp.{PlatformAccess, QuarryPlus}
import net.minecraft.core.HolderLookup
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.data.PackOutput
import net.minecraft.data.recipes.{RecipeCategory, RecipeOutput, RecipeProvider, ShapedRecipeBuilder}
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.alchemy.{PotionContents, Potions}
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.item.{Item, Items}

import java.util.concurrent.CompletableFuture

class Recipe(ingredientProvider: IngredientProvider, output: PackOutput, registries: CompletableFuture[HolderLookup.Provider]) extends RecipeProvider(output, registries) {
  private val ip = ingredientProvider

  override def buildRecipes(recipeOutput: RecipeOutput): Unit = {
    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, PlatformAccess.getAccess.registerObjects.markerBlock.get)
      .define('g', ip.glowStoneDust)
      .define('l', ip.lapis)
      .define('r', Items.REDSTONE_TORCH)
      .pattern("glg")
      .pattern("lrl")
      .pattern("gl ")
      .unlockedBy(Items.REDSTONE_TORCH)
      .save(recipeOutput)

    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, PlatformAccess.getAccess.registerObjects().flexibleMarkerBlock().get())
      .define('g', Items.GREEN_DYE)
      .define('m', PlatformAccess.getAccess.registerObjects.markerBlock.get)
      .pattern("ggg")
      .pattern("mmm")
      .unlockedBy(PlatformAccess.getAccess.registerObjects.markerBlock.get)
      .unlockedBy(Items.GREEN_DYE)
      .save(recipeOutput)

    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, PlatformAccess.getAccess.registerObjects().flexibleMarkerBlock().get())
      .define('g', Items.GREEN_DYE)
      .define('m', PlatformAccess.getAccess.registerObjects.chunkMarkerBlock.get)
      .pattern("ggg")
      .pattern(" m ")
      .unlockedBy(PlatformAccess.getAccess.registerObjects.chunkMarkerBlock.get)
      .unlockedBy(Items.GREEN_DYE)
      .save(recipeOutput, modLoc(FlexibleMarkerBlock.NAME + "_from_" + ChunkMarkerBlock.NAME))

    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, PlatformAccess.getAccess.registerObjects().chunkMarkerBlock().get())
      .define('r', ip.redStoneDust)
      .define('m', PlatformAccess.getAccess.registerObjects.markerBlock.get)
      .pattern("rrr")
      .pattern("mmm")
      .unlockedBy(PlatformAccess.getAccess.registerObjects.markerBlock.get)
      .unlockedBy(Items.REDSTONE)
      .save(recipeOutput)

    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, PlatformAccess.getAccess.registerObjects().chunkMarkerBlock().get())
      .define('r', ip.redStoneDust)
      .define('m', PlatformAccess.getAccess.registerObjects.flexibleMarkerBlock.get)
      .pattern("rrr")
      .pattern(" m ")
      .unlockedBy(PlatformAccess.getAccess.registerObjects.flexibleMarkerBlock.get)
      .unlockedBy(Items.REDSTONE)
      .save(recipeOutput, modLoc(ChunkMarkerBlock.NAME + "_from_" + FlexibleMarkerBlock.NAME))

    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, PlatformAccess.getAccess.registerObjects().moverBlock().get())
      .define('d', ip.diamond)
      .define('a', Items.ANVIL)
      .define('g', ip.goldIngot)
      .define('i', ip.ironIngot)
      .define('o', ip.obsidian)
      .define('m', ip.marker)
      .pattern("md ")
      .pattern("igi")
      .pattern("aoa")
      .unlockedBy(Items.ANVIL)
      .unlockedBy(ip.markerTag)
      .unlockedBy(ip.obsidianTag)
      .save(recipeOutput)

    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, PlatformAccess.getAccess.registerObjects().quarryBlock().get())
      .define('D', Items.DROPPER)
      .define('R', ip.redStoneBlock)
      .define('g', ip.pickaxeForQuarry)
      .define('i', ip.ironIngot)
      .define('o', ip.obsidian)
      .define('m', ip.marker)
      .pattern("ioi")
      .pattern("gDg")
      .pattern("iRm")
      .unlockedBy(ip.markerTag)
      .save(recipeOutput)

    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, quarryItem("status_checker"))
      .define('g', ip.glass)
      .define('i', ip.ironIngot)
      .define('m', ip.marker)
      .pattern("ggg")
      .pattern("imi")
      .unlockedBy(ip.markerTag)
      .save(recipeOutput)

    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, quarryItem("y_setter"))
      .define('g', ip.glass)
      .define('i', ip.lapis)
      .define('m', ip.marker)
      .pattern("ggg")
      .pattern("imi")
      .unlockedBy(ip.markerTag)
      .save(recipeOutput)

    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, quarryItem("pump_module"))
      .define('g', ip.glass)
      .define('d', Items.GREEN_DYE)
      .define('b', Items.LAVA_BUCKET)
      .define('r', ip.redStoneDust)
      .define('G', ip.goldBlock)
      .define('m', ip.marker)
      .pattern("dgd")
      .pattern("gbg")
      .pattern("rGm")
      .unlockedBy(Items.GREEN_DYE)
      .unlockedBy(ip.markerTag)
      .save(ip.pumpModuleRecipeOutput(recipeOutput))

    val bedrockModule = quarryItem("remove_bedrock_module")
    {
      val builder = ShapedRecipeBuilder.shaped(RecipeCategory.MISC, bedrockModule)
      builder
        .define('o', ip.obsidian)
        .define('m', ip.marker)
      if (PlatformAccess.getAccess.platformName().equalsIgnoreCase("fabric")) {
        builder
          .pattern("ooo")
          .pattern(" m ")
          .pattern(" m ")
      } else {
        builder
          .define('d', ip.diamondBlock)
          .pattern("ooo")
          .pattern("dmd")
          .pattern("dmd")
      }
      builder
        .unlockedBy(PlatformAccess.getAccess.registerObjects().quarryBlock().get())
        .unlockedBy(ip.obsidianTag)
        .save(recipeOutput)
    }

    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, quarryItem(ExpModuleItem.NAME))
      .define('h', Items.HAY_BLOCK)
      .define('e', ip.enderPearl)
      .define('G', ip.goldBlock)
      .define('m', ip.marker)
      .define('p', Items.POTION)
      .pattern("epe")
      .pattern("mhp")
      .pattern("GhG")
      .unlockedBy(ip.markerTag)
      .save(ip.expModuleRecipeOutput(recipeOutput))

    InstallBedrockModuleRecipe.builder(PlatformAccess.getAccess.registerObjects().quarryBlock().get())
      .unlockedBy("has_bedrock_module", RecipeProvider.has(bedrockModule))
      .save(ip.installBedrockModuleQuarryRecipeOutput(recipeOutput), ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, "install_bedrock_module_quarry"))

    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, quarryItem(RepeatTickModuleItem.NAME))
      .define('a', ip.amethyst)
      .define('p', ip.prismarineShard)
      .define('m', ip.marker)
      .define('w', Ingredient.of(PotionContents.createItemStack(Items.LINGERING_POTION, Potions.STRONG_SWIFTNESS)))
      .pattern("apa")
      .pattern("pwp")
      .pattern("apm")
      .unlockedBy(ip.markerTag)
      .save(ip.repeatTickModuleRecipeOutput(recipeOutput))

    ShapedRecipeBuilder.shaped(RecipeCategory.MISC, PlatformAccess.getAccess.registerObjects().advQuarryBlock().get())
      .define('d', ip.diamondBlock)
      .define('e', ip.emeraldBlock)
      .define('q', PlatformAccess.getAccess.registerObjects().quarryBlock().get())
      .define('i', Items.ENDER_EYE)
      .define('h', Items.DRAGON_HEAD)
      .define('s', ip.netherStar)
      .pattern("dhd")
      .pattern("qsq")
      .pattern("eie")
      .unlockedBy(PlatformAccess.getAccess.registerObjects().quarryBlock().get())
      .save(recipeOutput)
  }

  private def quarryItem(name: String): Item = {
    BuiltInRegistries.ITEM.get(modLoc(name))
  }

  private def modLoc(name: String): ResourceLocation = {
    ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, name)
  }
}
