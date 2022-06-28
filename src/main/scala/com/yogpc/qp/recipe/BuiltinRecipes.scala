package com.yogpc.qp.recipe

import com.yogpc.qp.QuarryPlusI._
import com.yogpc.qp.block._
import com.yogpc.qp.item.{ItemTemplate, ItemTool}
import com.yogpc.qp.tile._
import com.yogpc.qp.utils.IngredientWithCount
import com.yogpc.qp.{Config, QuarryPlus}
import net.minecraft.block.Block
import net.minecraft.init.Blocks._
import net.minecraft.init.Items._
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.util.ResourceLocation

object BuiltinRecipes {

  private def F(item: Item, count: Double, damage: Int): Int => ItemStack = level => new ItemStack(item, (count * level).toInt, damage)

  private def F(item: Item, count: Double): Int => ItemStack = F(item, count, 0)

  private def F(block: Block, count: Double): Int => ItemStack = F(Item.getItemFromBlock(block), count)

  def registerRecipes(): Unit = {
    val map = Map(
      BlockController.SYMBOL -> (ItemDamage(blockController), 1000000, Seq(F(NETHER_STAR, 1), F(GOLD_INGOT, 40), F(IRON_INGOT, 40), F(ROTTEN_FLESH, 20), F(ARROW, 20), F(BONE, 20), F(GUNPOWDER, 20), F(GHAST_TEAR, 5), F(MAGMA_CREAM, 10), F(BLAZE_ROD, 14), F(CARROT, 2), F(POTATO, 2))),
      TileMiningWell.SYMBOL -> (ItemDamage(blockMiningWell), 160000, Seq(F(DIAMOND, 1), F(GOLD_INGOT, 3), F(IRON_INGOT, 16), F(REDSTONE, 8), F(ENDER_PEARL, 1), F(NETHER_STAR, 1d / 25d))),
      BlockBreaker.SYMBOL -> (ItemDamage(blockBreaker), 320000, Seq(F(DIAMOND, 12), F(GOLD_INGOT, 16), F(IRON_INGOT, 32), F(REDSTONE, 32), F(ENDER_PEARL, 1))),
      TileLaser.SYMBOL -> (ItemDamage(blockLaser), 640000, Seq(F(DIAMOND, 8), F(GOLD_INGOT, 16), F(REDSTONE, 96), F(GLOWSTONE_DUST, 32), F(OBSIDIAN, 16), F(GLASS, 72), F(ENDER_PEARL, 1d / 5d))),
      TileAdvQuarry.SYMBOL -> (ItemDamage(blockChunkDestroyer), 3200000, Seq(F(blockQuarry2, 3d / 2d), F(blockPump, 1), F(itemTool, 1, 1), F(blockMarker, 3d / 2d), F(DIAMOND_BLOCK, 4), F(EMERALD_BLOCK, 4), F(ENDER_EYE, 32), F(NETHER_STAR, 1), F(net.minecraft.init.Items.SKULL, 24d / 25d, 5))),
      TileAdvPump.SYMBOL -> (ItemDamage(blockStandalonePump), 3200000, Seq(F(blockPump, 1), F(blockMiningWell, 1), F(blockMarker, 3d / 2d))),
      BlockMover.SYMBOL -> (ItemDamage(blockMover), 320000, Seq(F(DIAMOND, 16), F(GOLD_INGOT, 4), F(IRON_INGOT, 4), F(REDSTONE, 24), F(OBSIDIAN, 32), F(ANVIL, 1), F(NETHER_STAR, 1d / 25d), F(ENDER_PEARL, 1))),
      BlockPlacer.SYMBOL -> (ItemDamage(blockPlacer), 320000, Seq(F(DIAMOND, 12), F(GOLD_INGOT, 32), F(IRON_INGOT, 16), F(REDSTONE, 32), F(ENDER_PEARL, 1))),
      Symbol("PumpPlus") -> (ItemDamage(blockPump), 320000, Seq(F(GOLD_INGOT, 8), F(IRON_INGOT, 24), F(REDSTONE, 32), F(GLASS, 256), F(CACTUS, 40), F(NETHER_STAR, 1d / 25d), F(ENDER_PEARL, 2d / 5d))),
      TileMarker.SYMBOL -> (ItemDamage(blockMarker), 20000, Seq(F(GOLD_INGOT, 7d / 2d), F(IRON_INGOT, 4), F(REDSTONE, 6), F(DYE, 6, 4), F(GLOWSTONE_DUST, 2), F(ENDER_PEARL, 2d / 5d))),
      TileRefinery.SYMBOL -> (ItemDamage(blockRefinery), 640000, Seq(F(DIAMOND, 18), F(GOLD_INGOT, 12), F(IRON_INGOT, 12), F(GLASS, 64), F(REDSTONE, 16), F(ANVIL, 1), F(OBSIDIAN, 12), F(NETHER_STAR, 1d / 25d), F(ENDER_PEARL, 4d / 5d))),
      TileQuarry2.SYMBOL -> (ItemDamage(blockQuarry2), 320000, Seq(F(DIAMOND, 16), F(GOLD_INGOT, 16), F(IRON_INGOT, 32), F(REDSTONE, 8), F(ENDER_PEARL, 2), F(NETHER_STAR, 3d / 25d))),
      BlockBookMover.SYMBOL -> (ItemDamage(blockBookMover), 500000, Seq(F(blockMover, 2), F(BEACON, 1), F(BOOKSHELF, 64), F(DIAMOND, 8))),
      BlockExpPump.SYMBOL -> (ItemDamage(blockExpPump), 320000, Seq(F(GOLD_INGOT, 8), F(IRON_INGOT, 24), F(REDSTONE, 32), F(EXPERIENCE_BOTTLE, 1), F(HAY_BLOCK, 16), F(NETHER_STAR, 1d / 25d), F(ENDER_PEARL, 1))),
      TileReplacer.SYMBOL -> (ItemDamage(blockReplacer), 6400000, Seq(F(WATER_BUCKET, 16), F(LAVA_BUCKET, 16), F(IRON_INGOT, 8), F(GOLD_INGOT, 16), F(REDSTONE, 8), F(ENDER_PEARL, 2), F(ENDER_EYE, 6), F(net.minecraft.init.Items.SKULL, 24d / 25d, 5), F(NETHER_STAR, 4), F(STONE, 512))),
      TileFiller.SYMBOL -> (ItemDamage(blockFiller), 160000, Seq(F(IRON_INGOT, 16), F(LADDER, 16), F(IRON_AXE, 3)))
    )
    map.filterKeys(Config.content.enableMap).foreach {
      case (s, (item, energy, recipe)) => WorkbenchRecipe.addSeqRecipe(item, energy, recipe, name = s)
    }
    if (Config.content.enableMap.contains(TileQuarry.SYMBOL) &&
      Config.content.enableMap.contains(TileQuarry2.SYMBOL)) {
      val r = new ResourceLocation(QuarryPlus.modID, "convert_quarry_plus")
      WorkbenchRecipe.addEnchantmentCopyRecipe(r, new ItemStack(blockQuarry2), 1000, new IngredientWithCount(new ItemStack(blockQuarry)))
    }

    val list1 = Seq(
      (ItemDamage(magicMirror, 1), 32000, Seq(F(ENDER_EYE, 8), F(magicMirror, 1))),
      (ItemDamage(magicMirror, 2), 32000, Seq(F(ENDER_EYE, 8), F(magicMirror, 1), F(OBSIDIAN, 4), F(DIRT, 8), F(PLANKS, 8))),
      (ItemDamage(itemTool, 0), 80000, Seq(F(DIAMOND, 2), F(GOLD_INGOT, 8), F(IRON_INGOT, 12), F(REDSTONE, 16), F(DYE, 4, 4), F(OBSIDIAN, 2), F(ENDER_PEARL, 3d / 25d))),
      (ItemDamage(ItemTool.getEditorStack), 160000, Seq(F(DIAMOND, 2), F(IRON_INGOT, 8), F(REDSTONE, 2), F(DYE, 8), F(BOOK, 32), F(FEATHER, 1), F(ENDER_PEARL, 1d / 5d))),
      (ItemDamage(itemTool, 2), 320000, Seq(F(IRON_INGOT, 32), F(LAVA_BUCKET, 6d / 5d), F(WATER_BUCKET, 6d / 5d), F(ENDER_PEARL, 3d / 25d))),
      (ItemDamage(itemTool, 3), 80000, Seq(F(GOLD_INGOT, 16), F(REPEATER, 8), F(COMPARATOR, 4), F(QUARTZ, 32))),
      (ItemDamage(ItemTemplate.getTemplateStack), 80000, Seq(F(itemTool, 1, 1), F(CHEST, 2))),
      (ItemDamage(torchModule), 640000, Seq(F(TORCH, 512), F(DISPENSER, 8), F(DAYLIGHT_DETECTOR, 32), F(COMPARATOR, 32))),
      (ItemDamage(fuelModuleNormal), 3200, Seq(F(FURNACE, 1.5D), F(GOLD_BLOCK, 8)))
    )
    list1.foreach { case (result, e, recipe) => WorkbenchRecipe.addSeqRecipe(result, e, recipe) }

    if (QuarryPlus.instance().inDev) {
      // Add debug recipe to check JEI work
      WorkbenchRecipe.addIngredientRecipe(
        new ResourceLocation(QuarryPlus.modID, "test_1"),
        new ItemStack(DIAMOND_BLOCK, 64),
        164253.25,
        Seq(IRON_INGOT, GOLD_INGOT, QUARTZ, REDSTONE, EMERALD, ENDER_PEARL, WHEAT_SEEDS, EXPERIENCE_BOTTLE, COMPARATOR, COMPASS).map(i => IngredientWithCount.getSeq(new ItemStack(i))) ++
          Seq(STONE, NETHERRACK, DIRT, WOOL, IRON_BLOCK, GOLD_BLOCK, QUARTZ_BLOCK, REDSTONE_BLOCK, EMERALD_BLOCK, DISPENSER, HAY_BLOCK).map(i => IngredientWithCount.getSeq(new ItemStack(i))),
        hardCode = true, showInJEI = true
      )
    }
  }
}
