package com.yogpc.qp.recipe

import com.yogpc.qp.tile.ItemDamage
import com.yogpc.qp.utils.IngredientWithCount
import net.minecraft.item.ItemStack
import net.minecraft.util.ResourceLocation

import scala.collection.JavaConverters.asScalaBufferConverter

final class EnchantmentCopyRecipe(override val location: ResourceLocation, o: ItemStack, e: Double, input: IngredientWithCount)
  extends WorkbenchRecipe(ItemDamage(o), e, showInJEI = true) {
  override val size = 1

  override def inputs: Seq[Seq[IngredientWithCount]] = Seq(Seq(input))

  override def getOutput(inputStacks: java.util.List[ItemStack]): ItemStack = {
    val stack = super.getOutput
    val tagFrom = inputStacks.asScala.find(input.matches)
    tagFrom.flatMap(s => Option(s.getTagCompound))
      .map(_.copy())
      .map { t =>
        if (stack.hasTagCompound) t.merge(stack.getTagCompound)
        t
      }.foreach(t => stack.setTagCompound(t))
    stack
  }
}
