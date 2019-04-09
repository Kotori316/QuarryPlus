package com.yogpc.qp

import com.yogpc.qp.tile.APowerTile
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.datafix.IFixableData
import net.minecraftforge.common.util.Constants.NBT

object FixData {

  object EnergyNBTFix extends IFixableData {
    override val getFixVersion = 1

    override def fixTagCompound(nbt: NBTTagCompound): NBTTagCompound = {
      if (QuarryPlusI.tileIdSet.contains(nbt.getString("id"))) {
        import APowerTile._
        if (nbt.hasKey(NBT_MAX_STORED, NBT.TAG_DOUBLE)) {
          val stored = nbt.getDouble(NBT_STORED_ENERGY)
          val receive = nbt.getDouble(NBT_MAX_RECEIVE)
          val capacity = nbt.getDouble(NBT_MAX_STORED)
          nbt.setLong(NBT_STORED_ENERGY, (stored * MicroJtoMJ).toLong)
          nbt.setLong(NBT_MAX_RECEIVE, (receive * MicroJtoMJ).toLong)
          nbt.setLong(NBT_MAX_STORED, (capacity * MicroJtoMJ).toLong)
        }
      }
      nbt
    }
  }

}
