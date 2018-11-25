package com.yogpc.qp.integration.jei

import java.util.{Collections, ArrayList => AList, List => JList}

import com.yogpc.qp.tile.WorkbenchRecipes
import mezz.jei.api.ingredients.IIngredients
import mezz.jei.api.recipe.IRecipeWrapper
import net.minecraft.client.Minecraft
import net.minecraft.item.ItemStack

import scala.collection.JavaConverters._

class WorkBenchRecipeWrapper(recipe: WorkbenchRecipes) extends IRecipeWrapper with Ordered[WorkBenchRecipeWrapper] {

  override def getIngredients(ingredients: IIngredients): Unit = {
    val inputs = new AList[JList[ItemStack]](recipeSize)

    recipe.inputs.foreach(inputs add Collections.singletonList(_))
    val outputs = Collections.singletonList(recipe.output.toStack())

    ingredients.setInputLists(classOf[ItemStack], inputs)
    ingredients.setOutputs(classOf[ItemStack], outputs)
  }

  override def drawInfo(minecraft: Minecraft, recipeWidth: Int, recipeHeight: Int, mouseX: Int, mouseY: Int): Unit = {
    import WorkBenchRecipeCategory._
    minecraft.fontRenderer.drawString(getEnergyRequired.toString + "MJ", 40 - xOff, 74 - yOff, 0x404040)
  }

  val getEnergyRequired: Double = recipe.energy
  val recipeSize: Int = recipe.size

  override def compare(that: WorkBenchRecipeWrapper): Int = java.lang.Double.compare(getEnergyRequired, that.getEnergyRequired)
}

object WorkBenchRecipeWrapper {

  def getAll: JList[WorkBenchRecipeWrapper] = {
    WorkbenchRecipes.getRecipeMap.collect { case (_, recipe) if recipe.showInJEI => new WorkBenchRecipeWrapper(recipe) }.toList.sorted.asJava
  }
}
