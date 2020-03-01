package com.yogpc.qp

import com.yogpc.qp.utils.Holder
import net.minecraft.item.{ItemGroup, ItemStack}
import net.minecraftforge.api.distmarker.{Dist, OnlyIn}

class CreativeTabQuarryPlus extends ItemGroup(QuarryPlus.Mod_Name) {
  setTabPath(QuarryPlus.modID)

  @OnlyIn(Dist.CLIENT)
  override def createIcon() = new ItemStack(Holder.blockQuarry)
}
