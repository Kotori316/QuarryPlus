package com.kotori316.test_qp.recipe

import cats.implicits._
import com.kotori316.test_qp.InitMC
import com.yogpc.qp.machines.workbench.{IngredientRecipe, IngredientWithCount}
import com.yogpc.qp.utils.ItemElement
import net.minecraft.item.{ItemStack, Items}
import net.minecraft.util.ResourceLocation
import org.junit.jupiter.api.Assertions.{assertAll, assertEquals, assertFalse, assertTrue}
import org.junit.jupiter.api.Test

private[recipe] class IngredientRecipeTest extends InitMC {

  @Test
  def createInstanceTest(): Unit = {
    val recipe = new IngredientRecipe(new ResourceLocation("quarry_test:recipe1"), new ItemStack(Items.APPLE), 1000, true,
      Seq(Seq(new IngredientWithCount(new ItemStack(Items.OAK_SAPLING, 16)))))

    assertAll(
      () => assertTrue(recipe.output === ItemElement(new ItemStack(Items.APPLE))),
      () => assertEquals(Items.APPLE, recipe.getOutput.getItem),
      () => assertTrue(recipe.hasAllRequiredItems(List(new ItemStack(Items.OAK_SAPLING, 16)))),
      () => assertTrue(recipe.hasAllRequiredItems(List(new ItemStack(Items.OAK_SAPLING, 32)))),
      () => assertFalse(recipe.hasAllRequiredItems(List(new ItemStack(Items.OAK_SAPLING, 8)))),
      () => assertFalse(recipe.hasAllRequiredItems(List(new ItemStack(Items.ACACIA_BUTTON, 16)))),
      () => assertFalse(recipe.hasAllRequiredItems(List())),
    )
  }
}
