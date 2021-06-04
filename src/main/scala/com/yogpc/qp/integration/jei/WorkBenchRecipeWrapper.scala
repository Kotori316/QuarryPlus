package com.yogpc.qp.integration.jei

import com.yogpc.qp.recipe.{RecipeSearcher, WorkbenchRecipe}

import java.util.{Collections, ArrayList => AList, List => JList}
import mezz.jei.api.IJeiRuntime
import mezz.jei.api.ingredients.{IIngredients, VanillaTypes}
import mezz.jei.api.recipe.IRecipeWrapper
import net.minecraft.client.Minecraft
import net.minecraft.item.ItemStack

import scala.collection.JavaConverters._

class WorkBenchRecipeWrapper(val recipe: WorkbenchRecipe) extends IRecipeWrapper with Ordered[WorkBenchRecipeWrapper] {

  override def getIngredients(ingredients: IIngredients): Unit = {
    val inputs = new AList[JList[ItemStack]](recipeSize)

    recipe.inputs.foreach(l => inputs add l.flatMap(_.stackList).asJava)
    val outputs = Collections.singletonList(recipe.getOutput)

    ingredients.setInputLists(VanillaTypes.ITEM, inputs)
    ingredients.setOutputs(VanillaTypes.ITEM, outputs)
  }

  override def drawInfo(minecraft: Minecraft, recipeWidth: Int, recipeHeight: Int, mouseX: Int, mouseY: Int): Unit = {
    import WorkBenchRecipeCategory._
    minecraft.fontRenderer.drawString(getEnergyRequired.toString + "MJ", 36 - xOff, 70 - yOff, 0x404040)
  }

  val getEnergyRequired: Double = recipe.energy
  val recipeSize: Int = recipe.size

  override def compare(that: WorkBenchRecipeWrapper): Int = java.lang.Double.compare(getEnergyRequired, that.getEnergyRequired)

  def canEqual(other: Any): Boolean = other.isInstanceOf[WorkBenchRecipeWrapper]

  override def equals(other: Any): Boolean = other match {
    case that: WorkBenchRecipeWrapper =>
      recipe == that.recipe
    case _ => false
  }

  override def hashCode: Int = recipe.hashCode

  override def toString = s"${recipe.getClass.getSimpleName} ${recipe.getOutput.getDisplayName}"
}

object WorkBenchRecipeWrapper {

  def getAll: JList[WorkBenchRecipeWrapper] = {
    RecipeSearcher.getDefault.getRecipeMap.collect { case (_, recipe) if recipe.showInJEI => new WorkBenchRecipeWrapper(recipe) }.toList.sorted.asJava
  }

  def hideRecipe(runtime: IJeiRuntime): Unit = {
    val recipeSeq = RecipeSearcher.getDefault.getRecipeMap.values.toList.filter(_.hasContent)
    val registry = runtime.getRecipeRegistry
    registry.getRecipeWrappers(QuarryJeiPlugin.workBenchRecipeCategory).asScala
      .filter(r => !recipeSeq.contains(r.recipe))
      .foreach(registry.hideRecipe(_, WorkBenchRecipeCategory.UID))
  }
}
