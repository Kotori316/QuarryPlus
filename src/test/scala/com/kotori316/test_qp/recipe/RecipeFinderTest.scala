package com.kotori316.test_qp.recipe

import com.kotori316.test_qp.InitMC
import com.yogpc.qp.machines.workbench.{RecipeFinder, WorkbenchRecipes}
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation
import org.junit.jupiter.api.Assertions.{assertAll, assertFalse, assertTrue}
import org.junit.jupiter.api.Test

private[recipe] final class RecipeFinderTest extends InitMC {
  @Test
  def emptyRecipeFinder(): Unit = {
    val finder = RecipeFinderTest.TestFinder(Map.empty)
    assertAll(
      () => assertTrue(finder.getRecipe(Seq.empty).isEmpty),
      () => assertFalse(finder.getRecipeFromResult(ItemStack.EMPTY).isPresent),
    )
  }
}

private[recipe] object RecipeFinderTest {

  final case class TestFinder(override val recipes: Map[ResourceLocation, WorkbenchRecipes]) extends RecipeFinder

}
