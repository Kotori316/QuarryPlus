package com.yogpc.qp

import net.minecraft.creativetab.CreativeTabs
import net.minecraft.item.ItemStack

class CreativeTabQuarryPlus extends CreativeTabs(QuarryPlus.Mod_Name) {

    override def getTabIconItem = new ItemStack(QuarryPlusI.blockQuarry)

}