package com.yogpc.qp.test

import com.google.gson.JsonObject
import com.yogpc.qp.machines.workbench.WorkbenchRecipes
import com.yogpc.qp.utils.Holder
import net.minecraft.enchantment.{EnchantmentHelper, Enchantments}
import net.minecraft.item.ItemStack
import net.minecraft.util.{JSONUtils, ResourceLocation}
import org.junit.jupiter.api.Assertions._
import org.junit.jupiter.api.Test

import scala.jdk.javaapi.CollectionConverters

private[test] class RecipeTest {

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
    )
  }

  @Test
  def copyEnchantmentTest(): Unit = {
    val recipes = WorkbenchRecipes.Serializer.read(new ResourceLocation("quarryplus:convert_quarry"), json)
    val oldQuarry = new ItemStack(Holder.blockQuarry)
    EnchantmentHelper.setEnchantments(
      CollectionConverters.asJava(Map(Enchantments.SILK_TOUCH -> Int.box(1), Enchantments.EFFICIENCY -> Int.box(4)))
      , oldQuarry)
    val out = recipes.getOutput(CollectionConverters.asJava(List(oldQuarry)))
    assertFalse(out.isEmpty, "Out is valid item.")
    assertAll(
      () => assertEquals(1, EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, out), "Silktouch"),
      () => assertEquals(4, EnchantmentHelper.getEnchantmentLevel(Enchantments.EFFICIENCY, out), "Efficiency"),
    )
  }
}
