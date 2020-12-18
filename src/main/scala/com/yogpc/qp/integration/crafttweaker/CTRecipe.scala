package com.yogpc.qp.integration.crafttweaker

import com.blamejared.crafttweaker.CraftTweaker
import com.blamejared.crafttweaker.api.item.{IIngredient, IItemStack}
import com.yogpc.qp.machines.base.APowerTile
import com.yogpc.qp.machines.workbench.{EnchantmentCopyRecipe, IngredientRecipe, IngredientWithCount, WorkbenchRecipes}
import net.minecraft.util.ResourceLocation

protected object CTRecipe {
  def createWorkbenchRecipe(recipeName: String, output: IItemStack, oneInput: IIngredient, energy: Float): WorkbenchRecipes =
    createRecipeInternal(recipeName, output, Seq(Seq(oneInput)), energy)

  def createWorkbenchRecipe(recipeName: String, output: IItemStack, inputs: Array[IIngredient], energy: Float): WorkbenchRecipes =
    createRecipeInternal(recipeName, output, inputs.toSeq.map(Seq(_)), energy)

  def createWorkbenchRecipe(recipeName: String, output: IItemStack, inputs: Array[Array[IIngredient]], energy: Float): WorkbenchRecipes =
    createRecipeInternal(recipeName, output, inputs.toSeq.map(_.toSeq), energy)

  def createEnchantmentCopyRecipe(recipeName: String, output: IItemStack, copyFrom: Array[IIngredient], inputs: Array[Array[IIngredient]], energy: Float): WorkbenchRecipes = {
    new EnchantmentCopyRecipe(
      new ResourceLocation(CraftTweaker.MODID, recipeName),
      output.getInternal,
      (energy * APowerTile.FEtoMicroJ).toLong,
      copyFrom.toSeq.map(getRecipePart),
      inputs.toSeq.map(_.toSeq.map(getRecipePart))
    )
  }

  private def createRecipeInternal(recipeName: String, output: IItemStack, inputs: Seq[Seq[IIngredient]], energy: Float): WorkbenchRecipes = {
    new IngredientRecipe(
      new ResourceLocation(CraftTweaker.MODID, recipeName),
      output.getInternal,
      (energy * APowerTile.FEtoMicroJ).toLong,
      true,
      inputs.map(_.map(getRecipePart))
    )
  }

  private def getRecipePart(ingredient: IIngredient): IngredientWithCount = {
    ingredient match {
      case stack: IItemStack => new IngredientWithCount(stack.getInternal)
      case g => IngredientWithCount(g.asVanillaIngredient(), 1)
    }
  }
}
