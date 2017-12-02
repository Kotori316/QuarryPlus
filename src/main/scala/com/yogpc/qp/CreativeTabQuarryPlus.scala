package com.yogpc.qp

import net.minecraft.creativetab.CreativeTabs

class CreativeTabQuarryPlus extends CreativeTabs(QuarryPlus.Mod_Name) {

    //    override def getTabIconItem = new ItemStack(QuarryPlusI.blockQuarry)
    override def getTabIconItem = QuarryPlusI.blockQuarry.itemBlock
}