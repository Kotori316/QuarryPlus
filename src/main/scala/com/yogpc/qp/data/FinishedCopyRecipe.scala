package com.yogpc.qp.data

import com.google.gson.JsonObject
import com.yogpc.qp.machines.workbench.{EnchantmentCopyRecipe, IngredientWithCount, WorkbenchRecipes}
import com.yogpc.qp.utils.ItemElement
import net.minecraft.data.IFinishedRecipe
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.IRecipeSerializer
import net.minecraft.util.ResourceLocation

case class FinishedCopyRecipe(location: String, o: ItemStack, energy: Double, in1: IngredientWithCount, inputs: Seq[IngredientWithCount]) extends IFinishedRecipe {
  override def serialize(json: JsonObject): Unit = {
    json.addProperty("id", location)
    json.addProperty("sub_type", EnchantmentCopyRecipe.subName)
    json.add("enchantment_from", in1.serializeJson)
    json.add("ingredients", SerializeUtils.serializeIngredients(inputs))
    json.addProperty("energy", energy)
    json.add("result", ItemElement(o).serializeJson)
  }

  override def getID: ResourceLocation = new ResourceLocation(location)

  override def getSerializer: IRecipeSerializer[_] = WorkbenchRecipes.Serializer

  override def getAdvancementJson: JsonObject = null

  override def getAdvancementID: ResourceLocation = null
}
