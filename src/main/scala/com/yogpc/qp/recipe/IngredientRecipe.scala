package com.yogpc.qp.recipe

import com.yogpc.qp.tile.ItemDamage
import com.yogpc.qp.utils.IngredientWithCount
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation

final class IngredientRecipe(override val location: ResourceLocation, o: ItemStack, e: Double, s: Boolean, seq: Seq[Seq[IngredientWithCount]],
                             override val hardCode: Boolean = false) extends WorkbenchRecipe(ItemDamage(o), e, s) {
  override val size = seq.size

  override def inputs: Seq[Seq[IngredientWithCount]] = seq

  override def getOutput: ItemStack = o.copy()
}
