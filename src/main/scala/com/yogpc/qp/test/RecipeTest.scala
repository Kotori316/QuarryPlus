package com.yogpc.qp.test

import java.util.Arrays.asList

import com.google.gson.JsonObject
import com.yogpc.qp.QuarryPlus
import com.yogpc.qp.machines.workbench.{EnchantmentCopyRecipe, IngredientWithCount, WorkbenchRecipes}
import com.yogpc.qp.utils.Holder
import net.minecraft.enchantment.{EnchantmentHelper, Enchantments}
import net.minecraft.item.{ItemStack, Items}
import net.minecraft.nbt.CompoundNBT
import net.minecraft.util.{JSONUtils, ResourceLocation}
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test

import scala.jdk.javaapi.CollectionConverters

private[test] final class RecipeTest {

  private[this] def id(s: String) = new ResourceLocation(QuarryPlus.modID, s.toLowerCase)

  val json: JsonObject = JSONUtils.fromJson(
    """
      |{
      |  "type": "quarryplus:workbench_recipe",
      |  "id": "quarryplus:convert_quarry",
      |  "sub_type": "copy_enchantment",
      |  "enchantment_from": {
      |    "item": "quarryplus:quarryplus",
      |    "count": 1
      |  },
      |  "ingredients": [ ],
      |  "energy": 1000.0,
      |  "result": {
      |    "item": "quarryplus:quarry",
      |    "count": 1
      |  },
      |  "conditions": [
      |    {
      |      "value": "NewQuarry",
      |      "type": "quarryplus:machine_enabled"
      |    },
      |    {
      |      "value": "QuarryPlus",
      |      "type": "quarryplus:machine_enabled"
      |    }
      |  ]
      |}""".stripMargin)

  @Test
  def convertRecipe(): Unit = {
    val recipes = WorkbenchRecipes.Serializer.read(new ResourceLocation("quarryplus:convert_quarry"), json)
    assertAll(
      () => assertEquals(new ResourceLocation("quarryplus:convert_quarry"), recipes.getId),
      () => assertTrue(recipes.hasContent, "Valid recipe"),
      () => assertEquals(Holder.blockQuarry2.asItem(), recipes.getOutput.getItem),
      () => assertEquals(1, recipes.getOutput.getCount),
      () => assertEquals(classOf[EnchantmentCopyRecipe], recipes.getClass, "Class check"),
      () => assertEquals(1, recipes.inputs.head.size),
    )
  }

  @Test
  def copyEnchantmentTest(): Unit = {
    val recipes = WorkbenchRecipes.Serializer.read(new ResourceLocation("quarryplus:convert_quarry"), json)
    val oldQuarry = new ItemStack(Holder.blockQuarry)
    EnchantmentHelper.setEnchantments(
      CollectionConverters.asJava(Map(Enchantments.SILK_TOUCH -> Int.box(1), Enchantments.EFFICIENCY -> Int.box(4)))
      , oldQuarry)
    val out = recipes.getOutput(asList(oldQuarry))
    assertFalse(out.isEmpty, "Out is valid item.")
    assertAll(
      () => assertEquals(1, EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, out), s"Silktouch in $out"),
      () => assertEquals(4, EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, out), s"Efficiency in $out"),
    )
  }

  @Test
  def copyNbtTest(): Unit = {
    val recipeOut = new ItemStack(Items.APPLE)
    locally {
      val tag = new CompoundNBT()
      tag.putString("test1", "a")
      recipeOut.setTag(tag)
    }
    val recipe = new EnchantmentCopyRecipe(id("d"), recipeOut, 100, List(new IngredientWithCount(new ItemStack(Items.OAK_LOG, 4))), Nil)
    locally {
      val ins = asList(new ItemStack(Items.OAK_LOG, 2), new ItemStack(Items.OAK_LOG, 7))
      val o = recipe.getOutput(ins)
      assertTrue(ItemStack.areItemStacksEqual(recipeOut, o))
    }
    locally {
      val in1 = new ItemStack(Items.OAK_LOG, 2)
      in1.addEnchantment(Enchantments.EFFICIENCY, 3)
      val in2 = new ItemStack(Items.OAK_LOG, 7)
      val t = new CompoundNBT()
      t.putByte("test2", 4)
      in2.setTag(t)
      val ins = asList(in1, in2)
      val o = recipe.getOutput(ins)
      val tag = o.getTag
      assertAll(
        () => assertEquals("a", tag.getString("test1")),
        () => assertEquals(4, tag.getByte("test2")),
      )
    }
  }

  @Test
  def gotErrorFromNoResult(): Unit = {
    val noResult = JSONUtils.fromJson(
      """
        |{
        |  "type": "quarryplus:workbench_recipe",
        |  "ingredients": [
        |    {
        |      "tag": "forge:ingots/gold",
        |      "count": 16
        |    }
        |  ],
        |  "energy": 320000.0,
        |  "showInJEI": true
        |}
        |""".stripMargin)
    val noResultRecipe = WorkbenchRecipes.parse(noResult, id("no_result"))
    assertAll(
      () => assertTrue(noResultRecipe.isLeft, "no result"),
      () => assertTrue(noResultRecipe.left.exists(_.contains("Missing result")), s"Exception $noResultRecipe")
    )
    val hasEmptyResult = new JsonObject
    noResult.entrySet().forEach(e => hasEmptyResult.add(e.getKey, e.getValue))
    hasEmptyResult.add("result", new JsonObject)
    val resultRecipe = WorkbenchRecipes.parse(hasEmptyResult, id("empty_result"))
    assertTrue(resultRecipe.isLeft, s"empty result $resultRecipe")
  }
}
