package com.kotori316.test_qp.recipe

import com.kotori316.test_qp.InitMC
import com.yogpc.qp.machines.workbench.{IngredientRecipe, IngredientWithCount}
import net.minecraft.item.crafting.Ingredient
import net.minecraft.item.{ItemStack, Items}
import org.junit.jupiter.api.Assertions.{assertAll, assertFalse, assertTrue}
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

private[recipe] final class RecipeMatchTest extends InitMC {

  @ParameterizedTest
  @MethodSource(Array("com.kotori316.test_qp.recipe.RecipeMatchTest#items"))
  def oneItemsMatchTest(stack: ItemStack): Unit = {
    val recipe = new IngredientRecipe(InitMC.id("oneItemsMatchTest"), stack, 10L, true,
      Seq(IngredientWithCount.getSeq(stack))
    )
    assertTrue(recipe.hasAllRequiredItems(Seq(stack)), s"Recipe of $recipe")
  }

  @ParameterizedTest
  @MethodSource(Array("com.kotori316.test_qp.recipe.RecipeMatchTest#items2"))
  def oneItemsMatchTest2(stack: ItemStack, other: ItemStack): Unit = {
    val recipe = new IngredientRecipe(InitMC.id("oneItemsMatchTest"), stack, 10L, true,
      Seq(IngredientWithCount.getSeq(stack))
    )
    assertTrue(recipe.hasAllRequiredItems(Seq(stack, other)), s"Recipe of $recipe")
    assertTrue(recipe.hasAllRequiredItems(Seq(other, stack)), s"Recipe of $recipe")
  }

  @ParameterizedTest
  @MethodSource(Array("com.kotori316.test_qp.recipe.RecipeMatchTest#items2"))
  def twoItemsMatchTest(stack: ItemStack, other: ItemStack): Unit = {
    val recipe = new IngredientRecipe(InitMC.id("oneItemsMatchTest"), stack, 10L, true,
      Seq(IngredientWithCount.getSeq(stack), IngredientWithCount.getSeq(other))
    )
    assertAll(
      () => assertFalse(recipe.hasAllRequiredItems(Seq(stack)), s"Recipe of $recipe"),
      () => assertFalse(recipe.hasAllRequiredItems(Seq(other)), s"Recipe of $recipe"),
      () => assertTrue(recipe.hasAllRequiredItems(Seq(stack, other)), s"Recipe of $recipe"),
      () => assertTrue(recipe.hasAllRequiredItems(Seq(other, stack)), s"Recipe of $recipe"),
    )
  }

  @Test
  def twoSameItemInRecipe1(): Unit = {
    val recipe = new IngredientRecipe(InitMC.id("oneItemsMatchTest"), new ItemStack(Items.STONE), 10L, true,
      Seq(Seq(IngredientWithCount(Ingredient.fromItems(Items.ACACIA_FENCE, Items.ACACIA_BUTTON), 1)),
        IngredientWithCount.getSeq(new ItemStack(Items.ACACIA_BUTTON, 1))
      ))
    assertAll(
      () => assertTrue(recipe.hasAllRequiredItems(Seq(new ItemStack(Items.ACACIA_FENCE), new ItemStack(Items.ACACIA_BUTTON))), recipe.toString),
      () => assertFalse(recipe.hasAllRequiredItems(Seq(new ItemStack(Items.ACACIA_FENCE), new ItemStack(Items.ACACIA_FENCE))), recipe.toString),
      () => assertFalse(recipe.hasAllRequiredItems(Seq(new ItemStack(Items.ACACIA_FENCE, 2))), recipe.toString),
      () => assertTrue(recipe.hasAllRequiredItems(Seq(new ItemStack(Items.ACACIA_BUTTON), new ItemStack(Items.ACACIA_BUTTON))), recipe.toString),
      () => assertTrue(recipe.hasAllRequiredItems(Seq(new ItemStack(Items.ACACIA_BUTTON, 2))), recipe.toString),
    )
  }

  @Test
  def twoSameItemInRecipe2(): Unit = {
    val recipe = new IngredientRecipe(InitMC.id("oneItemsMatchTest"), new ItemStack(Items.STONE), 10L, true,
      Seq(
        IngredientWithCount.getSeq(new ItemStack(Items.ACACIA_FENCE)) ++ IngredientWithCount.getSeq(new ItemStack(Items.ACACIA_BUTTON)),
        IngredientWithCount.getSeq(new ItemStack(Items.ACACIA_BUTTON))
      ))
    assertAll(
      () => assertTrue(recipe.hasAllRequiredItems(Seq(new ItemStack(Items.ACACIA_FENCE), new ItemStack(Items.ACACIA_BUTTON))), recipe.toString),
      () => assertFalse(recipe.hasAllRequiredItems(Seq(new ItemStack(Items.ACACIA_FENCE), new ItemStack(Items.ACACIA_FENCE))), recipe.toString),
      () => assertFalse(recipe.hasAllRequiredItems(Seq(new ItemStack(Items.ACACIA_FENCE, 2))), recipe.toString),
      () => assertTrue(recipe.hasAllRequiredItems(Seq(new ItemStack(Items.ACACIA_BUTTON), new ItemStack(Items.ACACIA_BUTTON))), recipe.toString),
      () => assertTrue(recipe.hasAllRequiredItems(Seq(new ItemStack(Items.ACACIA_BUTTON, 2))), recipe.toString),
    )
  }

  @Test
  def twoSameItemInRecipe3(): Unit = {
    val recipe = new IngredientRecipe(InitMC.id("oneItemsMatchTest"), new ItemStack(Items.STONE), 10L, true,
      Seq(
        IngredientWithCount.getSeq(new ItemStack(Items.ACACIA_FENCE, 2)) ++ IngredientWithCount.getSeq(new ItemStack(Items.ACACIA_BUTTON, 4)),
        IngredientWithCount.getSeq(new ItemStack(Items.ACACIA_BUTTON))
      ))
    assertAll(
      () => assertTrue(recipe.hasAllRequiredItems(Seq(new ItemStack(Items.ACACIA_FENCE, 2), new ItemStack(Items.ACACIA_BUTTON))), recipe.toString),
      () => assertFalse(recipe.hasAllRequiredItems(Seq(new ItemStack(Items.ACACIA_FENCE, 2), new ItemStack(Items.ACACIA_FENCE))), recipe.toString),
      () => assertFalse(recipe.hasAllRequiredItems(Seq(new ItemStack(Items.ACACIA_FENCE, 5))), recipe.toString),
      () => assertTrue(recipe.hasAllRequiredItems(Seq(new ItemStack(Items.ACACIA_BUTTON, 4), new ItemStack(Items.ACACIA_BUTTON))), recipe.toString),
      () => assertTrue(recipe.hasAllRequiredItems(Seq(new ItemStack(Items.ACACIA_BUTTON, 6))), recipe.toString),
      () => assertFalse(recipe.hasAllRequiredItems(Seq(new ItemStack(Items.ACACIA_BUTTON, 4))), recipe.toString),
    )
  }
}

object RecipeMatchTest {
  def items(): Array[_] = {
    Seq(Items.STONE, Items.BOOK, Items.DIAMOND_PICKAXE).map(new ItemStack(_)).toArray
  }

  def items2(): Array[_] = {
    Seq(Items.STONE, Items.BOOK, Items.DIAMOND_PICKAXE).map(new ItemStack(_))
      .combinations(2)
      .map(_.toArray)
      .toArray
  }
}
