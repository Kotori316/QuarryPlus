package com.yogpc.qp.utils

import net.minecraft.block.Block
import net.minecraft.block.state.IBlockState
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.ResourceLocation
import net.minecraftforge.fml.common.registry.ForgeRegistries
import net.minecraftforge.oredict.OreDictionary

object BlockData extends INBTReadable[BlockData] {
  final val Name_NBT = "name"
  final val Meta_NBT = "meta"
  final val BlockData_NBT = "blockdata"

  override def readFromNBT(nbt: NBTTagCompound): BlockData = {
    if (!nbt.hasKey(BlockData_NBT)) Invalid
    else {
      val compound = nbt.getCompoundTag(BlockData_NBT)
      new BlockData(compound.getString(Name_NBT), compound.getInteger(Meta_NBT))
    }
  }

  val Invalid: BlockData = new BlockData("Unknown:Dummy", 0) {
    override def equals(o: Any) = false

    override def hashCode = 0

    override def writeToNBT(nbt: NBTTagCompound): NBTTagCompound = nbt

    override def toString = "BlockData@Invaild"

    override def getLocalizedName = "Unknown:Dummy"
  }

  val comparator = Ordering.by((b: BlockData) => b.name) thenComparing Ordering.by((b: BlockData) => b.meta)
}

case class BlockData(name: ResourceLocation, meta: Int) extends INBTWritable with Ordered[BlockData] {

  def this(resourceName: String, meta: Int) {
    this(new ResourceLocation(resourceName), meta)
  }

  def this(block: Block, state: IBlockState) {
    this(ForgeRegistries.BLOCKS.getKey(block), state.getBlock.getMetaFromState(state))
  }

  override def equals(o: Any): Boolean = {
    o match {
      case data: BlockData =>
        name == data.name &&
          (meta == data.meta || meta == OreDictionary.WILDCARD_VALUE || data.meta == OreDictionary.WILDCARD_VALUE)
    }
  }

  override def hashCode: Int = this.name.hashCode

  def writeToNBT(nbt: NBTTagCompound): NBTTagCompound = {
    val compound = new NBTTagCompound
    compound.setString(BlockData.Name_NBT, name.toString)
    compound.setInteger(BlockData.Meta_NBT, meta)
    nbt.setTag(BlockData.BlockData_NBT, compound)
    nbt
  }

  override def toString: String = name + "@" + meta

  def getLocalizedName: String = {
    val sb = new StringBuilder
    sb.append(name)
    if (meta != OreDictionary.WILDCARD_VALUE) {
      sb.append(" : ").append(meta).append("  ")
      sb.append(Option(ForgeRegistries.BLOCKS.getValue(name)).fold("")(new ItemStack(_, 1, meta).getDisplayName))
    } else {
      sb.append("  ").append(Option(ForgeRegistries.BLOCKS.getValue(name)).fold("")(_.getLocalizedName))
    }
    sb.toString
  }

  override def compare(that: BlockData): Int = BlockData.comparator.compare(this, that)
}
