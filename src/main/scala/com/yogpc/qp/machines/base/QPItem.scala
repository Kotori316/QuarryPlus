package com.yogpc.qp.machines.base

import com.yogpc.qp.QuarryPlus
import com.yogpc.qp.machines.TranslationKeys
import com.yogpc.qp.utils.Holder
import net.minecraft.client.util.ITooltipFlag
import net.minecraft.item.{Item, ItemStack}
import net.minecraft.util.text.{ITextComponent, TranslationTextComponent}
import net.minecraft.world.World
import net.minecraftforge.api.distmarker.{Dist, OnlyIn}

class QPItem(name: String, propertySetter: Item.Properties => Item.Properties)
  extends Item(propertySetter(new Item.Properties().group(Holder.tab))) {
  setRegistryName(QuarryPlus.modID, name)

  def this(name: String) = {
    this(name, identity)
  }

  @OnlyIn(Dist.CLIENT)
  override def addInformation(stack: ItemStack, worldIn: World, tooltip: java.util.List[ITextComponent], flagIn: ITooltipFlag): Unit = {
    super.addInformation(stack, worldIn, tooltip, flagIn)
    this match {
      case d: IDisabled =>
        if (!d.enabled) {
          tooltip.add(new TranslationTextComponent(TranslationKeys.DISABLE_MESSAGE, new TranslationTextComponent(getTranslationKey(stack))))
        }
      case _ =>
    }
  }
}
