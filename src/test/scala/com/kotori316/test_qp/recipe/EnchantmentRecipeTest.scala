package com.kotori316.test_qp.recipe

import java.util.Collections

import com.kotori316.test_qp.InitMC
import com.yogpc.qp.machines.workbench.{EnchantmentCopyRecipe, IngredientWithCount, WorkbenchRecipes}
import net.minecraft.enchantment.{EnchantmentData, EnchantmentHelper, Enchantments}
import net.minecraft.item.{EnchantedBookItem, ItemStack, Items}
import net.minecraft.util.ResourceLocation
import org.junit.jupiter.api.Assertions.{assertAll, assertEquals, assertFalse, assertTrue}
import org.junit.jupiter.api.Test

import scala.util.chaining.scalaUtilChainingOps

private[recipe] class EnchantmentRecipeTest extends InitMC {
  @Test
  def moveFromEnchantedBook(): Unit = {
    val recipe = new EnchantmentCopyRecipe(new ResourceLocation("quarry_test:ert_1"), new ItemStack(Items.WHITE_WOOL), 10000,
      Seq(new IngredientWithCount(new ItemStack(Items.ENCHANTED_BOOK))), Seq())

    assertAll(
      () => assertFalse((recipe: WorkbenchRecipes).getOutput.hasTag, "Output for display"),
      () => assertFalse(recipe.getOutput(Collections.emptyList()).hasTag, "No acceptable input"),
      () => {
        val efficiencyInv = Collections.singletonList(EnchantedBookItem.getEnchantedItemStack(new EnchantmentData(Enchantments.EFFICIENCY, 5)))
        assertTrue(recipe.getOutput(efficiencyInv).hasTag)
        assertEquals(5, EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, recipe.getOutput(efficiencyInv)))
      },
      () => assertFalse(recipe.getOutput(Collections.singletonList(new ItemStack(Items.ENCHANTED_BOOK))).hasTag, "No enchantment input"),
    )
  }

  @Test
  def moveFromPickaxe(): Unit = {
    val recipe = new EnchantmentCopyRecipe(new ResourceLocation("quarry_test:ert_1"), new ItemStack(Items.WHITE_WOOL), 10000,
      Seq(new IngredientWithCount(new ItemStack(Items.DIAMOND_PICKAXE))), Seq())

    assertAll(
      () => assertFalse((recipe: WorkbenchRecipes).getOutput.hasTag, "Output for display"),
      () => assertFalse(recipe.getOutput(Collections.emptyList()).hasTag, "No acceptable input"),
      () => {
        val efficiencyInv = Collections.singletonList(new ItemStack(Items.DIAMOND_PICKAXE).tap(_.addEnchantment(Enchantments.EFFICIENCY, 5)))
        assertTrue(recipe.getOutput(efficiencyInv).hasTag)
        assertEquals(5, EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, recipe.getOutput(efficiencyInv)))
      },
      // Pickaxe has nbt to save damage.
      () => assertTrue(recipe.getOutput(Collections.singletonList(new ItemStack(Items.DIAMOND_PICKAXE))).hasTag, "No enchantment input"),
    )
  }

}
