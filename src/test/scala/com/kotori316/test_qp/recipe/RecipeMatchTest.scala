package com.kotori316.test_qp.recipe

import com.kotori316.test_qp.InitMC
import com.yogpc.qp.machines.workbench.{IngredientRecipe, IngredientWithCount}
import net.minecraft.item.crafting.Ingredient
import net.minecraft.item.{ItemStack, Items}
import org.junit.jupiter.api.Assertions.{assertAll, assertEquals, assertFalse, assertTrue}
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

private[recipe] final class RecipeMatchTest extends InitMC {

  @Test
  def normalRecipe1(): Unit = {
    val miningWellRecipe = new IngredientRecipe(
      InitMC.id("MiningWell"), new ItemStack(Items.MINECART), 1000L, true,
      Seq(IngredientWithCount(Ingredient.fromItems(Items.GOLD_INGOT), 3),
        IngredientWithCount(Ingredient.fromItems(Items.IRON_INGOT), 16),
        IngredientWithCount(Ingredient.fromItems(Items.REDSTONE), 8),
      ).map(i => Seq(i))
    )

    assertAll(Seq(
      Seq() -> false,
      Seq(new ItemStack(Items.GOLD_INGOT, 1), new ItemStack(Items.REDSTONE, 1)) -> false,
      Seq(new ItemStack(Items.GOLD_INGOT, 1), new ItemStack(Items.IRON_INGOT, 1), new ItemStack(Items.REDSTONE, 1)) -> false,
      Seq(new ItemStack(Items.GOLD_INGOT, 10), new ItemStack(Items.IRON_INGOT, 10), new ItemStack(Items.REDSTONE, 10)) -> false,
      Seq(new ItemStack(Items.GOLD_INGOT, 3), new ItemStack(Items.IRON_INGOT, 15), new ItemStack(Items.REDSTONE, 8)) -> false,
      Seq(new ItemStack(Items.GOLD_INGOT, 10), new ItemStack(Items.IRON_INGOT, 20), new ItemStack(Items.REDSTONE, 10)) -> true,
      Seq(new ItemStack(Items.IRON_INGOT, 20), new ItemStack(Items.REDSTONE, 10)) -> false,
      Seq(new ItemStack(Items.GOLD_INGOT, 10), new ItemStack(Items.IRON_INGOT, 20)) -> false,
      Seq(new ItemStack(Items.GOLD_INGOT, 3), new ItemStack(Items.IRON_INGOT, 16), new ItemStack(Items.REDSTONE, 8)) -> true,
      Seq(new ItemStack(Items.GOLD_INGOT, 3), new ItemStack(Items.IRON_INGOT, 16), new ItemStack(Items.REDSTONE, 8), new ItemStack(Items.STONE, 64)) -> true,
    ).map[Executable] { case (value, bool) => () => assertEquals(bool, miningWellRecipe.hasAllRequiredItems(value), s"Stacks = $value") }: _*)
  }

  @Test
  def stackedRecipe1(): Unit = {
    val recipe = new IngredientRecipe(InitMC.id("stackedRecipe1"), new ItemStack(Items.APPLE), 10L, true,
      Seq(IngredientWithCount.getSeq(new ItemStack(Items.OAK_SAPLING, 4))))
    assertAll(
      Seq(
        true -> Seq(new ItemStack(Items.OAK_SAPLING, 4)),
        true -> Seq(new ItemStack(Items.OAK_SAPLING, 16)),
        false -> Seq(new ItemStack(Items.OAK_SAPLING, 1)),
        false -> Seq(new ItemStack(Items.SPRUCE_SAPLING, 4)),
        false -> cats.Monoid.combineN(List(new ItemStack(Items.OAK_SAPLING, 1)), 4),
        false -> cats.Monoid.combineN(List(new ItemStack(Items.OAK_SAPLING, 3)), 4),
        true -> cats.Monoid.combineN(List(new ItemStack(Items.OAK_SAPLING, 4)), 4),
      ).map[Executable] { case (bool, value) => () =>
        assertEquals(bool, recipe.hasAllRequiredItems(value), s"From $value, expect${recipe.inputs}")
      }: _*
    )
  }

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
    assertAll(Seq(
      true -> Seq(new ItemStack(Items.ACACIA_FENCE), new ItemStack(Items.ACACIA_BUTTON)),
      false -> Seq(new ItemStack(Items.ACACIA_FENCE), new ItemStack(Items.ACACIA_FENCE)),
      false -> Seq(new ItemStack(Items.ACACIA_FENCE, 2)),
      true -> Seq(new ItemStack(Items.ACACIA_BUTTON), new ItemStack(Items.ACACIA_BUTTON)),
      true -> Seq(new ItemStack(Items.ACACIA_BUTTON, 2)),
    ).map[Executable] { case (bool, value) => () =>
      assertEquals(bool, recipe.hasAllRequiredItems(value), s"From $value, expect${recipe.inputs}")
    }: _*)
  }

  @Test
  def twoSameItemInRecipe2(): Unit = {
    val recipe = new IngredientRecipe(InitMC.id("oneItemsMatchTest"), new ItemStack(Items.STONE), 10L, true,
      Seq(
        IngredientWithCount.getSeq(new ItemStack(Items.ACACIA_FENCE)) ++ IngredientWithCount.getSeq(new ItemStack(Items.ACACIA_BUTTON)),
        IngredientWithCount.getSeq(new ItemStack(Items.ACACIA_BUTTON))
      ))
    assertAll(Seq(
      true -> Seq(new ItemStack(Items.ACACIA_FENCE), new ItemStack(Items.ACACIA_BUTTON)),
      false -> Seq(new ItemStack(Items.ACACIA_FENCE), new ItemStack(Items.ACACIA_FENCE)),
      false -> Seq(new ItemStack(Items.ACACIA_FENCE, 2)),
      false -> Seq.empty,
      true -> Seq(new ItemStack(Items.ACACIA_BUTTON), new ItemStack(Items.ACACIA_BUTTON)),
      true -> Seq(new ItemStack(Items.ACACIA_BUTTON, 2)),
    ).map[Executable] { case (bool, value) => () =>
      assertEquals(bool, recipe.hasAllRequiredItems(value), s"From $value, expect${recipe.inputs}")
    }: _*)
  }

  @Test
  def twoSameItemInRecipe3(): Unit = {
    val recipe = new IngredientRecipe(InitMC.id("oneItemsMatchTest"), new ItemStack(Items.STONE), 10L, true,
      Seq(
        IngredientWithCount.getSeq(new ItemStack(Items.ACACIA_FENCE, 2)) ++ IngredientWithCount.getSeq(new ItemStack(Items.ACACIA_BUTTON, 4)),
        IngredientWithCount.getSeq(new ItemStack(Items.ACACIA_BUTTON))
      ))
    assertAll(Seq(
      true -> Seq(new ItemStack(Items.ACACIA_FENCE, 2), new ItemStack(Items.ACACIA_BUTTON)),
      true -> Seq(new ItemStack(Items.ACACIA_FENCE, 5), new ItemStack(Items.ACACIA_BUTTON, 2)),
      false -> Seq(new ItemStack(Items.ACACIA_FENCE, 2), new ItemStack(Items.ACACIA_FENCE)),
      false -> Seq(new ItemStack(Items.ACACIA_FENCE, 5)),
      true -> Seq(new ItemStack(Items.ACACIA_BUTTON, 4), new ItemStack(Items.ACACIA_BUTTON)),
      false -> Seq(new ItemStack(Items.ACACIA_BUTTON, 3), new ItemStack(Items.ACACIA_BUTTON)),
      true -> Seq(new ItemStack(Items.ACACIA_BUTTON, 6)),
      false -> Seq(new ItemStack(Items.ACACIA_BUTTON, 4)),
    ).map[Executable] { case (bool, value) => () =>
      assertEquals(bool, recipe.hasAllRequiredItems(value), s"From $value, expect${recipe.inputs}")
    }: _*)
  }

  @Test
  def twoSameItemInRecipe4(): Unit = {
    val recipe = new IngredientRecipe(InitMC.id("oneItemsMatchTest"), new ItemStack(Items.STONE), 10L, true,
      Seq(
        IngredientWithCount.getSeq(new ItemStack(Items.ACACIA_BUTTON, 3)),
        IngredientWithCount.getSeq(new ItemStack(Items.ACACIA_FENCE, 2)) ++ IngredientWithCount.getSeq(new ItemStack(Items.ACACIA_BUTTON, 4)),
      ))

    assertAll(
      Seq(
        true -> Seq(new ItemStack(Items.ACACIA_FENCE, 2), new ItemStack(Items.ACACIA_BUTTON, 3)),
        true -> Seq(new ItemStack(Items.ACACIA_BUTTON, 7)),
        false -> Seq(new ItemStack(Items.ACACIA_BUTTON, 2)),
        false -> Seq(new ItemStack(Items.ACACIA_BUTTON, 3)),
        false -> Seq(new ItemStack(Items.ACACIA_BUTTON, 4)),
        false -> Seq(new ItemStack(Items.ACACIA_BUTTON, 6)),
        true -> Seq(new ItemStack(Items.ACACIA_BUTTON, 3), new ItemStack(Items.ACACIA_FENCE, 2)),
        true -> Seq(new ItemStack(Items.ACACIA_BUTTON, 4), new ItemStack(Items.ACACIA_FENCE, 2)),
        true -> Seq(new ItemStack(Items.ACACIA_BUTTON, 16), new ItemStack(Items.ACACIA_FENCE, 2)),
        //        true -> Seq(new ItemStack(Items.ACACIA_BUTTON, 4), new ItemStack(Items.ACACIA_BUTTON, 3)),
        true -> Seq(new ItemStack(Items.ACACIA_BUTTON, 3), new ItemStack(Items.ACACIA_BUTTON, 4)),
      ).map[Executable] { case (bool, value) => () => assertEquals(bool, recipe.hasAllRequiredItems(value), s"From $value, expect${recipe.inputs}") }: _*
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
