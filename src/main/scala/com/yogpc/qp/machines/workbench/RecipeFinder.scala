package com.yogpc.qp.machines.workbench

import cats.implicits._
import com.yogpc.qp.utils.ItemElement
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation

import scala.jdk.CollectionConverters._
import scala.jdk.OptionConverters._

trait RecipeFinder {
  def recipes: Map[ResourceLocation, WorkbenchRecipes]

  def recipeSize: Int = recipes.size

  def getRecipe(inputs: java.util.List[ItemStack]): java.util.List[WorkbenchRecipes] = {
    getRecipe(inputs.asScala).asJava
  }

  def getRecipe(inputs: scala.collection.Seq[ItemStack]): Seq[WorkbenchRecipes] = {
    val in = inputs.toSeq
    recipes.collect { case (_, recipe) if recipe.hasAllRequiredItems(in) => recipe }.toSeq
  }

  def getRecipeFromResult(stack: ItemStack): java.util.Optional[WorkbenchRecipes] = {
    if (stack.isEmpty) return java.util.Optional.empty()
    val id = ItemElement(stack)
    recipes.find { case (_, r) => r.output === id }.map(_._2).toJava
  }
}
