package com.yogpc.qp.data

import com.google.gson.{JsonArray, JsonObject}
import com.yogpc.qp.machines.workbench.{IngredientWithCount, WorkbenchRecipes}
import com.yogpc.qp.utils.ItemElement
import net.minecraft.data.IFinishedRecipe
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.IRecipeSerializer
import net.minecraft.util.ResourceLocation

class FinishedWorkbenchRecipe(location: String, o: ItemStack, energy: Double, showInJEI: Boolean, inputs: Seq[IngredientWithCount]) extends IFinishedRecipe {
  require(getID != null) // Check location is valid value.

  override def serialize(json: JsonObject): Unit = {
    json.addProperty("id", location)
    json.add("ingredients", serializeIngredients(inputs))
    json.addProperty("energy", energy)
    json.addProperty("showInJEI", showInJEI)
    json.add("result", ItemElement(o).serializeJson)
  }

  override def getID: ResourceLocation = new ResourceLocation(location)

  override def getSerializer: IRecipeSerializer[_] = WorkbenchRecipes.Serializer

  override def getAdvancementJson: JsonObject = null

  override def getAdvancementID: ResourceLocation = null

  def serializeIngredients(inputs: Seq[IngredientWithCount]): JsonArray = {
    inputs.foldLeft(new JsonArray) { case (a, i) =>
      a.add(i.serializeJson)
      a
    }
  }
}
