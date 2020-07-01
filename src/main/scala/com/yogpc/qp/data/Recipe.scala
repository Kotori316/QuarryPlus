package com.yogpc.qp.data

import java.util.Collections

import com.yogpc.qp.machines.advpump.TileAdvPump
import com.yogpc.qp.machines.advquarry.TileAdvQuarry
import com.yogpc.qp.machines.bookmover.BlockBookMover
import com.yogpc.qp.machines.controller.BlockController
import com.yogpc.qp.machines.exppump.BlockExpPump
import com.yogpc.qp.machines.item.{ItemListEditor, ItemTemplate}
import com.yogpc.qp.machines.marker.TileMarker
import com.yogpc.qp.machines.mini_quarry.MiniQuarryTile
import com.yogpc.qp.machines.mover.BlockMover
import com.yogpc.qp.machines.pb.PlacerTile
import com.yogpc.qp.machines.pump.TilePump
import com.yogpc.qp.machines.quarry.{BlockSolidQuarry, TileMiningWell, TileQuarry, TileQuarry2}
import com.yogpc.qp.machines.replacer.TileReplacer
import com.yogpc.qp.machines.workbench.{IngredientWithCount, TileWorkbench}
import com.yogpc.qp.utils.{EnableCondition, EnchantmentIngredient, Holder, QuarryConfigCondition}
import net.minecraft.data._
import net.minecraft.enchantment.{EnchantmentData, Enchantments}
import net.minecraft.item.crafting.Ingredient
import net.minecraft.item.{ItemStack, Items}
import net.minecraftforge.common.Tags
import net.minecraftforge.common.crafting.conditions.NotCondition

import scala.collection.mutable.ListBuffer

final class Recipe(f: DataGenerator) extends QuarryPlusDataProvider.DataProvider(f) {

  import QuarryPlusDataProvider.location

  final val MODULE_RECIPE_GROUP = "quarryplus:group_module"

  def workbenchRecipes: List[RecipeSerializeHelper] = {
    val buffer = new ListBuffer[RecipeSerializeHelper]
    // ADV_QUARRY
    buffer += RecipeSerializeHelper(new FinishedWorkbenchRecipe("quarryplus:builtin_chunkdestroyer", new ItemStack(Holder.blockAdvQuarry),
      3200000.0, true,
      Seq(IngredientWithCount(Ingredient.fromItems(Holder.blockQuarry, Holder.blockQuarry2), 3),
        new IngredientWithCount(new ItemStack(Holder.blockPump, 2)),
        new IngredientWithCount(new ItemStack(Holder.itemListEditor, 2)),
        new IngredientWithCount(new ItemStack(Holder.blockMarker, 3)),
        IngredientWithCount(Ingredient.fromTag(Tags.Items.STORAGE_BLOCKS_DIAMOND), 8),
        IngredientWithCount(Ingredient.fromTag(Tags.Items.STORAGE_BLOCKS_EMERALD), 8),
        new IngredientWithCount(new ItemStack(Items.ENDER_EYE, 64)),
        IngredientWithCount(Ingredient.fromTag(Tags.Items.NETHER_STARS), 2),
        new IngredientWithCount(new ItemStack(Items.DRAGON_HEAD, 1)),
      )), saveName = location("chunkdestroyer")).addCondition(new EnableCondition(TileAdvQuarry.SYMBOL))
    // Enchant Mover
    buffer += RecipeSerializeHelper(new FinishedWorkbenchRecipe("quarryplus:builtin_enchantmover", new ItemStack(Holder.blockMover),
      320000, true,
      Seq(IngredientWithCount(Ingredient.fromTag(Tags.Items.GEMS_DIAMOND), 32),
        IngredientWithCount(Ingredient.fromTag(Tags.Items.INGOTS_GOLD), 8),
        IngredientWithCount(Ingredient.fromTag(Tags.Items.INGOTS_IRON), 8),
        IngredientWithCount(Ingredient.fromTag(Tags.Items.DUSTS_REDSTONE), 48),
        IngredientWithCount(Ingredient.fromTag(Tags.Items.OBSIDIAN), 64),
        new IngredientWithCount(new ItemStack(Items.ANVIL, 2)),
        IngredientWithCount(Ingredient.fromTag(Tags.Items.ENDER_PEARLS), 2),
      )), saveName = location("enchantmover")).addCondition(new EnableCondition(BlockMover.SYMBOL))
    // Mover from Book
    buffer += RecipeSerializeHelper(new FinishedWorkbenchRecipe("quarryplus:builtin_mover_from_book", new ItemStack(Holder.blockBookMover),
      500000, true,
      Seq(new IngredientWithCount(new ItemStack(Holder.blockMover, 4)),
        new IngredientWithCount(new ItemStack(Items.BEACON, 2)),
        IngredientWithCount(Ingredient.fromTag(Tags.Items.BOOKSHELVES), 128),
        IngredientWithCount(Ingredient.fromTag(Tags.Items.GEMS_DIAMOND), 16),
      )), saveName = location("mover_from_book")).addCondition(new EnableCondition(BlockBookMover.SYMBOL))
    // Exp Pump
    buffer += RecipeSerializeHelper(new FinishedWorkbenchRecipe("quarryplus:builtin_exp_pump", new ItemStack(Holder.blockExpPump),
      320000, true,
      Seq(IngredientWithCount(Ingredient.fromTag(Tags.Items.INGOTS_GOLD), 16),
        IngredientWithCount(Ingredient.fromTag(Tags.Items.INGOTS_IRON), 48),
        IngredientWithCount(Ingredient.fromTag(Tags.Items.DUSTS_REDSTONE), 64),
        new IngredientWithCount(new ItemStack(Items.EXPERIENCE_BOTTLE, 2)),
        new IngredientWithCount(new ItemStack(Items.HAY_BLOCK, 32)),
        IngredientWithCount(Ingredient.fromTag(Tags.Items.ENDER_PEARLS), 2),
      )), saveName = location("exp_pump")).addCondition(new EnableCondition(BlockExpPump.SYMBOL))
    // Fuel Module
    buffer += RecipeSerializeHelper(new FinishedWorkbenchRecipe("quarryplus:builtin_item.fuel_module_normal", new ItemStack(Holder.itemFuelModuleNormal),
      3200, true,
      Seq(new IngredientWithCount(new ItemStack(Items.FURNACE, 3)),
        IngredientWithCount(Ingredient.fromTag(Tags.Items.STORAGE_BLOCKS_GOLD), 16),
      )), saveName = location("fuel_module")).addCondition(new EnableCondition(Holder.itemFuelModuleNormal.getSymbol))
    // List Editor
    buffer += RecipeSerializeHelper(new FinishedWorkbenchRecipe("quarryplus:builtin_item.listeditor", ItemListEditor.getEditorStack,
      160000, true,
      Seq(IngredientWithCount(Ingredient.fromTag(Tags.Items.GEMS_DIAMOND), 4),
        IngredientWithCount(Ingredient.fromTag(Tags.Items.INGOTS_IRON), 16),
        IngredientWithCount(Ingredient.fromTag(Tags.Items.DUSTS_REDSTONE), 4),
        new IngredientWithCount(new ItemStack(Items.INK_SAC, 16)),
        new IngredientWithCount(new ItemStack(Items.BOOK, 64)),
        IngredientWithCount(Ingredient.fromTag(Tags.Items.FEATHERS), 2),
      )), saveName = location("listeditor"))
    // Marker
    buffer += RecipeSerializeHelper(new FinishedWorkbenchRecipe("quarryplus:builtin_marker_plus", new ItemStack(Holder.blockMarker),
      20000, true,
      Seq(IngredientWithCount(Ingredient.fromTag(Tags.Items.INGOTS_GOLD), 7),
        IngredientWithCount(Ingredient.fromTag(Tags.Items.INGOTS_IRON), 8),
        IngredientWithCount(Ingredient.fromTag(Tags.Items.DUSTS_REDSTONE), 12),
        new IngredientWithCount(new ItemStack(Items.LAPIS_LAZULI, 12)),
        IngredientWithCount(Ingredient.fromTag(Tags.Items.DUSTS_GLOWSTONE), 4),
      )), saveName = location("marker_plus")).addCondition(new EnableCondition(TileMarker.SYMBOL))
    // Mining Well Plus
    buffer += RecipeSerializeHelper(new FinishedWorkbenchRecipe("quarryplus:builtin_miningwell_plus", new ItemStack(Holder.blockMiningWell),
      160000, true,
      Seq(IngredientWithCount(Ingredient.fromTag(Tags.Items.INGOTS_GOLD), 3),
        IngredientWithCount(Ingredient.fromTag(Tags.Items.INGOTS_IRON), 16),
        IngredientWithCount(Ingredient.fromTag(Tags.Items.DUSTS_REDSTONE), 8),
      )), saveName = location("miningwell_plus")).addCondition(new EnableCondition(TileMiningWell.SYMBOL))
    // Pump Plus
    buffer += RecipeSerializeHelper(new FinishedWorkbenchRecipe("quarryplus:builtin_pump_plus", new ItemStack(Holder.blockPump),
      320000, true,
      Seq(IngredientWithCount(Ingredient.fromTag(Tags.Items.INGOTS_GOLD), 16),
        IngredientWithCount(Ingredient.fromTag(Tags.Items.INGOTS_IRON), 48),
        IngredientWithCount(Ingredient.fromTag(Tags.Items.DUSTS_REDSTONE), 64),
        IngredientWithCount(Ingredient.fromTag(Tags.Items.GLASS_COLORLESS), 512),
        new IngredientWithCount(new ItemStack(Items.CACTUS, 80)),
      )), saveName = location("pump_plus")).addCondition(new EnableCondition(TilePump.SYMBOL))
    // Quarry Plus
    buffer += RecipeSerializeHelper(new FinishedWorkbenchRecipe("quarryplus:builtin_quarryplus", new ItemStack(Holder.blockQuarry2),
      320000, true,
      Seq(IngredientWithCount(Ingredient.fromTag(Tags.Items.GEMS_DIAMOND), 32),
        IngredientWithCount(Ingredient.fromTag(Tags.Items.INGOTS_GOLD), 32),
        IngredientWithCount(Ingredient.fromTag(Tags.Items.INGOTS_IRON), 64),
        IngredientWithCount(Ingredient.fromTag(Tags.Items.DUSTS_REDSTONE), 16),
        IngredientWithCount(Ingredient.fromTag(Tags.Items.ENDER_PEARLS), 4),
      )), saveName = location("quarryplus")).addCondition(new EnableCondition(TileQuarry2.SYMBOL))
    // Replacer
    buffer += RecipeSerializeHelper(new FinishedWorkbenchRecipe("quarryplus:builtin_replacer", new ItemStack(Holder.blockReplacer),
      6400000, true,
      Seq(new IngredientWithCount(new ItemStack(Items.WATER_BUCKET, 32)),
        new IngredientWithCount(new ItemStack(Items.LAVA_BUCKET, 32)),
        IngredientWithCount(Ingredient.fromTag(Tags.Items.INGOTS_GOLD), 16),
        IngredientWithCount(Ingredient.fromTag(Tags.Items.INGOTS_IRON), 32),
        IngredientWithCount(Ingredient.fromTag(Tags.Items.DUSTS_REDSTONE), 16),
        IngredientWithCount(Ingredient.fromTag(Tags.Items.ENDER_PEARLS), 4),
        new IngredientWithCount(new ItemStack(Items.ENDER_EYE, 12)),
        new IngredientWithCount(new ItemStack(Items.DRAGON_HEAD, 1)),
        IngredientWithCount(Ingredient.fromTag(Tags.Items.NETHER_STARS), 8),
        IngredientWithCount(Ingredient.fromTag(Tags.Items.STONE), 1024),
      )), saveName = location("replacer")).addCondition(new EnableCondition(TileReplacer.SYMBOL))
    // Remove Bedrock Module
    val diamond_pickaxe = new ItemStack(Items.DIAMOND_PICKAXE)
    diamond_pickaxe.removeChildTag("Damage")
    buffer += RecipeSerializeHelper(new FinishedWorkbenchRecipe("quarryplus:builtin_item.remove_bedrock_module", new ItemStack(Holder.itemRemoveBedrockModule),
      640000, true,
      Seq(IngredientWithCount(Ingredient.fromTag(Tags.Items.OBSIDIAN), 32),
        IngredientWithCount(Ingredient.fromTag(Tags.Items.STORAGE_BLOCKS_DIAMOND), 16),
        IngredientWithCount(new EnchantmentIngredient(diamond_pickaxe, Collections.singletonList(new EnchantmentData(Enchantments.SILK_TOUCH, 1))), 1),
      )), saveName = location("remove_bedrock_module"))
      .addCondition(new EnableCondition(Holder.itemRemoveBedrockModule.getSymbol))
      .addCondition(new QuarryConfigCondition("RemoveBedrock"))
    // Spawner Controller
    buffer += RecipeSerializeHelper(new FinishedWorkbenchRecipe("quarryplus:builtin_spawner_controller", new ItemStack(Holder.blockController),
      1000000, true,
      Seq(IngredientWithCount(Ingredient.fromTag(Tags.Items.NETHER_STARS), 2),
        IngredientWithCount(Ingredient.fromTag(Tags.Items.INGOTS_GOLD), 80),
        IngredientWithCount(Ingredient.fromTag(Tags.Items.INGOTS_IRON), 80),
        new IngredientWithCount(new ItemStack(Items.ROTTEN_FLESH, 40)),
        IngredientWithCount(Ingredient.fromTag(Tags.Items.ARROWS), 40),
        new IngredientWithCount(new ItemStack(Items.BONE, 40)),
        new IngredientWithCount(new ItemStack(Items.GUNPOWDER, 40)),
        new IngredientWithCount(new ItemStack(Items.GHAST_TEAR, 40)),
        new IngredientWithCount(new ItemStack(Items.MAGMA_CREAM, 40)),
        IngredientWithCount(Ingredient.fromTag(Tags.Items.RODS_BLAZE), 40),
        IngredientWithCount(Ingredient.fromTag(Tags.Items.CROPS_CARROT), 4),
        IngredientWithCount(Ingredient.fromTag(Tags.Items.CROPS_POTATO), 4),
      )), saveName = location("spawner_controller")).addCondition(new EnableCondition(BlockController.SYMBOL))
    // Advanced Pump
    buffer += RecipeSerializeHelper(new FinishedWorkbenchRecipe("quarryplus:builtin_advanced_pump", new ItemStack(Holder.blockAdvPump),
      3200000, true,
      Seq(new IngredientWithCount(new ItemStack(Holder.blockPump, 2)),
        new IngredientWithCount(new ItemStack(Holder.blockMiningWell, 2)),
        new IngredientWithCount(new ItemStack(Holder.blockMarker, 3)),
      )), saveName = location("standalone_pump")).addCondition(new EnableCondition(TileAdvPump.SYMBOL))
    // Status Checker
    buffer += RecipeSerializeHelper(new FinishedWorkbenchRecipe("quarryplus:builtin_statuschecker", new ItemStack(Holder.itemStatusChecker),
      80000, true,
      Seq(IngredientWithCount(Ingredient.fromTag(Tags.Items.INGOTS_GOLD), 16),
        IngredientWithCount(Ingredient.fromTag(Tags.Items.INGOTS_IRON), 24),
        IngredientWithCount(Ingredient.fromTag(Tags.Items.DUSTS_REDSTONE), 32),
        new IngredientWithCount(new ItemStack(Items.LAPIS_LAZULI, 8)),
        IngredientWithCount(Ingredient.fromTag(Tags.Items.OBSIDIAN), 4),
      )), saveName = location("statuschecker"))
    // Template
    buffer += RecipeSerializeHelper(new FinishedWorkbenchRecipe("quarryplus:builtin_item.template", ItemTemplate.getEditorStack,
      80000, true,
      Seq(new IngredientWithCount(new ItemStack(Holder.itemListEditor, 2)),
        IngredientWithCount(Ingredient.fromTag(Tags.Items.CHESTS), 4),
      )), saveName = location("template"))
    // Torch Module
    buffer += RecipeSerializeHelper(new FinishedWorkbenchRecipe("quarryplus:builtin_item.torch_module", new ItemStack(Holder.itemTorchModule),
      640000, true,
      Seq(new IngredientWithCount(new ItemStack(Items.TORCH, 1024)),
        new IngredientWithCount(new ItemStack(Items.DISPENSER, 16)),
        new IngredientWithCount(new ItemStack(Items.DAYLIGHT_DETECTOR, 64)),
        new IngredientWithCount(new ItemStack(Items.COMPARATOR, 64)),
      )), saveName = location("torch_module")).addCondition(new EnableCondition(Holder.itemTorchModule.getSymbol))
    // Y Setter
    buffer += RecipeSerializeHelper(new FinishedWorkbenchRecipe("quarryplus:builtin_item.y_setter", new ItemStack(Holder.itemYSetter),
      80000, true,
      Seq(IngredientWithCount(Ingredient.fromTag(Tags.Items.INGOTS_GOLD), 32),
        new IngredientWithCount(new ItemStack(Items.REPEATER, 16)),
        new IngredientWithCount(new ItemStack(Items.COMPARATOR, 8)),
        IngredientWithCount(Ingredient.fromTag(Tags.Items.GEMS_QUARTZ), 64),
      )), saveName = location("y_setter"))
    // Placer
    buffer += RecipeSerializeHelper(new FinishedWorkbenchRecipe("quarryplus:builtin_placer_plus", new ItemStack(Holder.blockPlacer),
      30000, true,
      Seq(new IngredientWithCount(new ItemStack(Items.DISPENSER, 1)),
        IngredientWithCount(Ingredient.fromTag(Tags.Items.INGOTS_GOLD), 2),
        IngredientWithCount(Ingredient.fromTag(Tags.Items.INGOTS_IRON), 1),
        IngredientWithCount(Ingredient.fromTag(Tags.Items.DUSTS_REDSTONE), 1),
        new IngredientWithCount(new ItemStack(Items.MOSSY_COBBLESTONE, 4)),
      )), saveName = location("placer_plus")).addCondition(new EnableCondition(PlacerTile.SYMBOL))
    // Mini Quarry
    buffer += RecipeSerializeHelper(new FinishedWorkbenchRecipe("quarryplus:builtin_mini_quarry", new ItemStack(Holder.blockMiniQuarry),
      10000, true,
      Seq(
        IngredientWithCount(Ingredient.fromTag(Tags.Items.GEMS_DIAMOND), 2),
        IngredientWithCount(Ingredient.fromTag(Tags.Items.INGOTS_GOLD), 16),
        IngredientWithCount(Ingredient.fromTag(Tags.Items.INGOTS_IRON), 32),
        IngredientWithCount(Ingredient.fromTag(Tags.Items.DUSTS_REDSTONE), 8),
        new IngredientWithCount(new ItemStack(Items.COMPARATOR, 4)),
      )), saveName = location("mini_quarry"), conditions = List(new EnableCondition(MiniQuarryTile.SYMBOL)))
    buffer.toList
  }

  def craftingRecipes: List[RecipeSerializeHelper] = {
    val EXP_PUMP_MODULE = RecipeSerializeHelper.by(
      ShapelessRecipeBuilder.shapelessRecipe(Holder.itemExpPumpModule)
        .addIngredient(Holder.blockExpPump)
        .setGroup(MODULE_RECIPE_GROUP),
      location("exp_pump_module"))
      .addCondition(new EnableCondition(Holder.itemExpPumpModule.getSymbol))

    val PUMP_MODULE = RecipeSerializeHelper.by(
      ShapelessRecipeBuilder.shapelessRecipe(Holder.itemPumpModule)
        .addIngredient(Holder.blockPump)
        .setGroup(MODULE_RECIPE_GROUP),
      location("pump_module"))
      .addCondition(new EnableCondition(Holder.itemPumpModule.getSymbol))
      .addCondition(new NotCondition(new EnableCondition(TileWorkbench.SYMBOL)))

    val REPLACER_MODULE = RecipeSerializeHelper.by(
      ShapelessRecipeBuilder.shapelessRecipe(Holder.itemReplacerModule)
        .addIngredient(Holder.blockReplacer)
        .setGroup(MODULE_RECIPE_GROUP),
      location("replacer_module"))
      .addCondition(new EnableCondition(Holder.itemReplacerModule.getSymbol))

    val REVERT_EXP_PUMP = RecipeSerializeHelper.by(
      ShapelessRecipeBuilder.shapelessRecipe(Holder.blockExpPump)
        .addIngredient(Holder.itemExpPumpModule)
        .setGroup(MODULE_RECIPE_GROUP),
      location("revert_exp_pump")
    ).addCondition(new EnableCondition(BlockExpPump.SYMBOL))
    val REVERT_PUMP = RecipeSerializeHelper.by(
      ShapelessRecipeBuilder.shapelessRecipe(Holder.blockPump)
        .addIngredient(Holder.itemPumpModule)
        .setGroup(MODULE_RECIPE_GROUP),
      location("revert_pump")
    ).addCondition(new EnableCondition(TilePump.SYMBOL))
      .addCondition(new NotCondition(new EnableCondition(TileWorkbench.SYMBOL)))
    val REVERT_REPLACER = RecipeSerializeHelper.by(
      ShapelessRecipeBuilder.shapelessRecipe(Holder.blockReplacer)
        .addIngredient(Holder.itemReplacerModule)
        .setGroup(MODULE_RECIPE_GROUP),
      location("revert_replacer")
    ).addCondition(new EnableCondition(TileReplacer.SYMBOL))

    val SOLID_QUARRY = RecipeSerializeHelper.by(
      ShapedRecipeBuilder.shapedRecipe(Holder.blockSolidQuarry)
        .patternLine("III")
        .patternLine("GDG")
        .patternLine("RRR")
        .key('D', Tags.Items.STORAGE_BLOCKS_GOLD)
        .key('R', Items.REDSTONE_TORCH)
        .key('I', Items.FURNACE)
        .key('G', Items.DIAMOND_PICKAXE),
      null
    ).addCondition(new EnableCondition(BlockSolidQuarry.SYMBOL))

    val WORKBENCH_PLUS = RecipeSerializeHelper.by(
      ShapedRecipeBuilder.shapedRecipe(Holder.blockWorkbench)
        .patternLine("III")
        .patternLine("GDG")
        .patternLine("RRR")
        .key('D', Tags.Items.STORAGE_BLOCKS_DIAMOND)
        .key('R', Items.REDSTONE)
        .key('I', Tags.Items.STORAGE_BLOCKS_IRON)
        .key('G', Tags.Items.STORAGE_BLOCKS_GOLD),
      null
    ).addCondition(new EnableCondition(TileWorkbench.SYMBOL))

    val PLACER_PLUS = RecipeSerializeHelper.by(
      ShapedRecipeBuilder.shapedRecipe(Holder.blockPlacer)
        .patternLine("GDG")
        .patternLine("MRM")
        .patternLine("MIM")
        .key('D', Items.DISPENSER)
        .key('R', Tags.Items.DUSTS_REDSTONE)
        .key('I', Tags.Items.INGOTS_IRON)
        .key('M', Items.MOSSY_COBBLESTONE)
        .key('G', Tags.Items.INGOTS_GOLD),
      saveName = location("placer_plus_crafting")
    ).addCondition(new NotCondition(new EnableCondition(TileWorkbench.SYMBOL)))
      .addCondition(new EnableCondition(PlacerTile.SYMBOL))
    EXP_PUMP_MODULE :: PUMP_MODULE :: REPLACER_MODULE ::
      REVERT_EXP_PUMP :: REVERT_PUMP :: REVERT_REPLACER ::
      SOLID_QUARRY :: WORKBENCH_PLUS :: PLACER_PLUS :: Nil
  }

  def enchantmentCopyRecipes: List[RecipeSerializeHelper] = {
    val buffer = new ListBuffer[RecipeSerializeHelper]
    // Convert quarry
    buffer += RecipeSerializeHelper(FinishedCopyRecipe("quarryplus:convert_quarry", new ItemStack(Holder.blockQuarry2), 1000d,
      IngredientWithCount(Ingredient.fromItems(Holder.blockQuarry), 1), Nil
    )).addCondition(new EnableCondition(TileQuarry.SYMBOL)).addCondition(new EnableCondition(TileQuarry2.SYMBOL))

    // Pump Module from Pump Plus
    buffer += RecipeSerializeHelper(FinishedCopyRecipe("quarryplus:pump_module_workbench", new ItemStack(Holder.itemPumpModule), 1000d,
      IngredientWithCount(Ingredient.fromItems(Holder.blockPump), 1), Nil
    )).addCondition(new EnableCondition(Holder.itemPumpModule.getSymbol))

    // Pump Plus from Pump Module
    buffer += RecipeSerializeHelper(FinishedCopyRecipe("quarryplus:revert_pump_workbench", new ItemStack(Holder.blockPump), 1000d,
      IngredientWithCount(Ingredient.fromItems(Holder.itemPumpModule), 1), Nil
    )).addCondition(new EnableCondition(TilePump.SYMBOL))
    buffer.result()
  }

  override def data: List[RecipeSerializeHelper] = workbenchRecipes ::: craftingRecipes ::: enchantmentCopyRecipes

  override def directory = "recipes"
}
