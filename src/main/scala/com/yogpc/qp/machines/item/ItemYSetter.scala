package com.yogpc.qp.machines.item

import com.yogpc.qp.QuarryPlus
import com.yogpc.qp.utils.Holder
import net.minecraft.item.Item

class ItemYSetter extends Item((new Item.Properties).group(Holder.tab)) {
  setRegistryName(QuarryPlus.modID, QuarryPlus.Names.ySetter)
}
