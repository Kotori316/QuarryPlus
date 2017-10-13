package com.yogpc.qp.tile

import com.yogpc.qp.Config
import com.yogpc.qp.item.ItemTool
import com.yogpc.qp.version.VersionUtil
import net.minecraft.item.ItemStack
import net.minecraftforge.items.ItemHandlerHelper
import net.minecraftforge.oredict.OreDictionary

import scala.collection.JavaConverters._
import scala.collection.mutable

class WorkbenchRecipes(val output: ItemDamage, val energy: Double, in: ItemStack*) {
    val size: Int = in.size

    def inputs = in.map(i => ItemHandlerHelper.copyStackWithSize(i, amount(i.getCount))).filter(VersionUtil.nonEmpty)

    def inputsJ() = inputs.asJava

    val amount: (Int => Int) = p => Math.floor(WorkbenchRecipes.difficulty * p / 50).toInt

    override def toString = s"WorkbenchRecipes(output=$output, energy=$energy)"

    override def hashCode(): Int = output.hashCode()

    override def equals(obj: scala.Any): Boolean = {
        if (super.equals(obj)) {
            return true
        }
        obj match {
            case r: WorkbenchRecipes => output == r.output && energy == r.energy
            case _ => false
        }
    }
}

object WorkbenchRecipes {
    var difficulty: Double = 2

    private val recipes = mutable.Map.empty[ItemDamage, WorkbenchRecipes]

    def recipeSize: Int = recipes.size

    /*
        addRecipe(ItemDamage(QuarryPlusI.blockChunkdestoryer), 3200000, MJ,
            new ItemStack(QuarryPlusI.blockQuarry, 3), new ItemStack(QuarryPlusI.blockPump, 2), new ItemStack(QuarryPlusI.itemPresetapplier, 1, OreDictionary.WILDCARD_VALUE),
            new ItemStack(QuarryPlusI.blockMarker, 3), new ItemStack(DIAMOND_BLOCK, 8), new ItemStack(EMERALD_BLOCK, 8), new ItemStack(ENDER_EYE, 64))
    */
    def addRecipe(output: ItemDamage, energy: Int, unit: EnergyUnit, inputs: ItemStack*): Unit = {
        val newRecipe = new WorkbenchRecipes(output, unit.multiple * energy, inputs: _*)
        recipes put(output, newRecipe)
    }

    def addRecipe(output: ItemDamage, energy: Int, inputs: ItemStack*): Unit = {
        addRecipe(output, energy, UnitMJ, inputs: _*)
    }

    def removeRecipe(output: ItemDamage): Unit = recipes.remove(output)

    def getRecipe(inputs: java.util.List[ItemStack]): java.util.List[WorkbenchRecipes] = {
        recipes.filter { case (_, workRecipe) =>
            workRecipe.inputs.forall(i => inputs.asScala.exists(t => OreDictionary.itemMatches(i, t, false) && t.getCount >= i.getCount))
        }.values.toList.asJava
    }

    def getRecipeMap: Map[ItemDamage, WorkbenchRecipes] = recipes.toMap

    def getRecipeFromResult(stack: ItemStack): java.util.Optional[WorkbenchRecipes] = {
        if (VersionUtil.isEmpty(stack)) return java.util.Optional.empty()
        recipes.get(ItemDamage(stack)).asJava
    }

    protected trait EnergyUnit {
        def multiple: Double
    }

    protected object UnitMJ extends EnergyUnit {
        override val multiple: Double = 1
    }

    protected object UnitRF extends EnergyUnit {
        override val multiple: Double = 0.1
    }

    def registerRecipes(): Unit = {
        import com.yogpc.qp.QuarryPlusI._
        import net.minecraft.init.Blocks._
        import net.minecraft.init.Items._
        addRecipe(ItemDamage(magicmirror, 1), 32000, new ItemStack(ENDER_EYE, 400), new ItemStack(magicmirror, 50))
        addRecipe(ItemDamage(magicmirror, 2), 32000, new ItemStack(ENDER_EYE, 400), new ItemStack(magicmirror, 50), new ItemStack(OBSIDIAN, 100), new ItemStack(DIRT, 200), new ItemStack(PLANKS, 200))
        if (!Config.content.disableController)
            addRecipe(ItemDamage(controller), 1000000, new ItemStack(NETHER_STAR, 50), new ItemStack(ROTTEN_FLESH, 1000), new ItemStack(ARROW, 1000), new ItemStack(BONE, 1000), new ItemStack(GUNPOWDER, 1000), new ItemStack(IRON_INGOT, 2000), new ItemStack(GOLD_INGOT, 1000), new ItemStack(GHAST_TEAR, 250), new ItemStack(MAGMA_CREAM, 500), new ItemStack(BLAZE_ROD, 700), new ItemStack(CARROT, 50), new ItemStack(POTATO, 50))
        addRecipe(ItemDamage(blockMarker), 20000, new ItemStack(REDSTONE, 300), new ItemStack(DYE, 300, 4), new ItemStack(GOLD_INGOT, 175), new ItemStack(IRON_INGOT, 150), new ItemStack(GLOWSTONE_DUST, 50), new ItemStack(ENDER_PEARL, 10))
        addRecipe(ItemDamage(blockQuarry), 320000, new ItemStack(DIAMOND, 800), new ItemStack(GOLD_INGOT, 800), new ItemStack(IRON_INGOT, 1600), new ItemStack(REDSTONE, 400), new ItemStack(ENDER_PEARL, 50), new ItemStack(NETHER_STAR, 3))
        addRecipe(ItemDamage(blockMover), 320000, new ItemStack(OBSIDIAN, 1600), new ItemStack(DIAMOND, 800), new ItemStack(ANVIL, 50), new ItemStack(REDSTONE, 1200), new ItemStack(GOLD_INGOT, 200), new ItemStack(IRON_INGOT, 200), new ItemStack(NETHER_STAR, 1), new ItemStack(ENDER_PEARL, 25))
        addRecipe(ItemDamage(blockMiningWell), 160000, new ItemStack(IRON_INGOT, 800), new ItemStack(REDSTONE, 400), new ItemStack(DIAMOND, 100), new ItemStack(ENDER_PEARL, 50), new ItemStack(NETHER_STAR, 1), new ItemStack(GOLD_INGOT, 25))
        addRecipe(ItemDamage(blockPump), 320000, new ItemStack(IRON_INGOT, 1200), new ItemStack(REDSTONE, 1600), new ItemStack(GLASS, 12800), new ItemStack(CACTUS, 2000), new ItemStack(GOLD_INGOT, 400), new ItemStack(NETHER_STAR, 1), new ItemStack(ENDER_PEARL, 10))
        if (false) addRecipe(ItemDamage(blockRefinery), 640000, new ItemStack(DIAMOND, 900), new ItemStack(GOLD_INGOT, 600), new ItemStack(IRON_INGOT, 600), new ItemStack(GLASS, 3200), new ItemStack(REDSTONE, 800), new ItemStack(ANVIL, 50), new ItemStack(OBSIDIAN, 600), new ItemStack(NETHER_STAR, 1), new ItemStack(ENDER_PEARL, 20))
        addRecipe(ItemDamage(itemTool, 0), 80000, new ItemStack(GOLD_INGOT, 400), new ItemStack(IRON_INGOT, 600), new ItemStack(OBSIDIAN, 100), new ItemStack(DIAMOND, 100), new ItemStack(REDSTONE, 400), new ItemStack(DYE, 100, 4), new ItemStack(ENDER_PEARL, 3))
        addRecipe(ItemDamage(ItemTool.getEditorStack), 160000, new ItemStack(IRON_INGOT, 400), new ItemStack(BOOK, 1600), new ItemStack(FEATHER, 50), new ItemStack(DYE, 400), new ItemStack(DIAMOND, 100), new ItemStack(REDSTONE, 100), new ItemStack(ENDER_PEARL, 3))
        addRecipe(ItemDamage(itemTool, 2), 320000, new ItemStack(IRON_INGOT, 1600), new ItemStack(LAVA_BUCKET, 60), new ItemStack(WATER_BUCKET, 60), new ItemStack(ENDER_PEARL, 3))
        addRecipe(ItemDamage(blockBreaker), 320000, new ItemStack(REDSTONE, 1600), new ItemStack(DIAMOND, 600), new ItemStack(GOLD_INGOT, 800), new ItemStack(IRON_INGOT, 1600), new ItemStack(ENDER_PEARL, 50))
        addRecipe(ItemDamage(blockPlacer), 320000, new ItemStack(REDSTONE, 1600), new ItemStack(DIAMOND, 600), new ItemStack(GOLD_INGOT, 1600), new ItemStack(IRON_INGOT, 800), new ItemStack(ENDER_PEARL, 50))
        if (false) addRecipe(ItemDamage(blockLaser), 640000, new ItemStack(DIAMOND, 400), new ItemStack(REDSTONE, 4800), new ItemStack(OBSIDIAN, 800), new ItemStack(GLASS, 3600), new ItemStack(GLOWSTONE_DUST, 1600), new ItemStack(GOLD_INGOT, 800), new ItemStack(ENDER_PEARL, 5))
    }
}