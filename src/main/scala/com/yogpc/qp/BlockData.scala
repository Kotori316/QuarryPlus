package com.yogpc.qp

import javax.annotation.{Nonnull, Nullable}

import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.common.registry.ForgeRegistries
import net.minecraftforge.oredict.OreDictionary

object BlockData {
    val Name_NBT = "name"
    val Meta_NBT = "meta"

    @Nonnull
    def of(compound: NBTTagCompound): BlockData = {
        if (compound == null) Invalid
        else new BlockData(compound.getString(Name_NBT), compound.getInteger(Meta_NBT))
    }

    val Invalid: BlockData = new BlockData("Unknown:Dummy", 0) {
        override def equals(o: Any) = false

        override def hashCode = 0

        override def toNBT = null

        override def toString = "BlockData@Invaild"

        override def getLocalizedName = "Unknown:Dummy"
    }
}

case class BlockData(name: ResourceLocation, meta: Int) {

    def this(resourceName: String, meta: Int) {
        this(new ResourceLocation(resourceName), meta)
    }

    override def equals(o: Any): Boolean = {
        o match {
            case data: BlockData =>
                name == data.name &&
                  (meta == data.meta || meta == OreDictionary.WILDCARD_VALUE || data.meta == OreDictionary.WILDCARD_VALUE)
        }
    }

    override def hashCode: Int = this.name.hashCode

    @Nullable
    def toNBT: NBTTagCompound = {
        val compound = new NBTTagCompound
        compound.setString(BlockData.Name_NBT, name.toString)
        compound.setInteger(BlockData.Meta_NBT, meta)
        compound
    }

    override def toString: String = name + "@" + meta

    def getLocalizedName: String = {
        val sb = new StringBuilder
        sb.append(name)
        if (meta != OreDictionary.WILDCARD_VALUE) {
            sb.append(":")
            sb.append(meta)
        }
        sb.append("  ")
        sb.append(Option(ForgeRegistries.BLOCKS.getValue(name)).map(_.getLocalizedName).getOrElse(name.toString))
        sb.toString
    }
}