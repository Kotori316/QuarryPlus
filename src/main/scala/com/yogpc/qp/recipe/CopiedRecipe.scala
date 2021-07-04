package com.yogpc.qp.recipe

import com.yogpc.qp.tile.ItemDamage
import com.yogpc.qp.utils.IngredientWithCount
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.Ingredient
import net.minecraft.network.PacketBuffer
import net.minecraft.util.ResourceLocation

import scala.collection.JavaConverters.setAsJavaSetConverter

class CopiedRecipe(o: ItemStack,
                   e: Double,
                   override val location: ResourceLocation,
                   override val inputs: Seq[Seq[IngredientWithCount]])
  extends WorkbenchRecipe(ItemDamage(o), e, false) {
  override val size = inputs.size

  override def getOutput: ItemStack = o

  def write(packet: PacketBuffer): Unit = {
    packet.writeItemStack(o)
    packet.writeResourceLocation(location)
    packet.writeDouble(energy)
    packet.writeVarInt(inputs.size)
    for (input <- inputs) {
      packet.writeVarInt(input.size)
      for (iwc <- input) {
        CopiedRecipe.writeIngredient(iwc.ingredient, packet)
        packet.writeInt(iwc.count)
      }
    }
  }
}

object CopiedRecipe {
  def makeCopy(searcher: RecipeSearcher): java.util.Set[CopiedRecipe] = {
    searcher.getRecipeMap
      .values
      .map(r => new CopiedRecipe(r.getOutput, r.energy, r.location, r.inputs))
      .toSet
      .asJava
  }

  private def writeIngredient(ingredient: Ingredient, buffer: PacketBuffer): Unit = {
    val stacks = ingredient.getMatchingStacks
    buffer.writeVarInt(stacks.length)
    stacks
      .map { i => val s = i.copy(); s.setCount(1); s }
      .foreach(buffer.writeItemStack)
  }

  def read(packet: PacketBuffer): CopiedRecipe = {
    val output = packet.readItemStack()
    val location = packet.readResourceLocation()
    val energy = packet.readDouble()
    val inputSize = packet.readVarInt()
    val inputs = for (_ <- Range(0, inputSize)) yield {
      val size = packet.readVarInt()
      for (_ <- Range(0, size)) yield
        IngredientWithCount(readIngredient(packet), packet.readInt())
    }
    new CopiedRecipe(output, energy, location, inputs)
  }

  private def readIngredient(buffer: PacketBuffer): Ingredient = {
    val size = buffer.readVarInt()
    val stacks = for (_ <- 0 until size) yield buffer.readItemStack()
    new StacksIngredient(stacks)
  }

  private class StacksIngredient(stacks: Seq[ItemStack]) extends Ingredient(stacks: _*)
}
