package com.kotori316.test_qp.recipe

import cats.implicits._
import com.kotori316.test_qp.InitMC
import com.yogpc.qp.machines.workbench.{IngredientRecipe, IngredientWithCount, WorkbenchRecipes}
import com.yogpc.qp.utils.ItemElement
import io.netty.buffer.Unpooled
import net.minecraft.item.{ItemStack, Items}
import net.minecraft.network.PacketBuffer
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

  @Test
  def bigSizeRecipe(): Unit = {
    val recipe = new IngredientRecipe(new ResourceLocation("quarry_test:recipe1"), new ItemStack(Items.DIAMOND), 320000, true,
      seq = Seq(
        IngredientWithCount.getSeq(new ItemStack(Items.STONE, 256)),
        IngredientWithCount.getSeq(new ItemStack(Items.COAL, 130))))

    assertTrue(recipe.hasAllRequiredItems(List(new ItemStack(Items.COAL, 512), new ItemStack(Items.STONE, 512))))
    val packet = new PacketBuffer(Unpooled.buffer())
    WorkbenchRecipes.Serializer.write(packet, recipe)
    val loaded = WorkbenchRecipes.Serializer.read(recipe.location, packet)
    assertTrue(loaded.hasAllRequiredItems(List(new ItemStack(Items.COAL, 512), new ItemStack(Items.STONE, 512))))
  }
}
