package com.yogpc.qp.machines.workbench

import java.util.Comparator

import net.minecraft.block.state.IBlockState
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.ResourceLocation
import net.minecraftforge.registries.ForgeRegistries

object BlockData {
  final val Name_NBT = "name"
  //  final val Meta_NBT = "meta"
  final val BlockData_NBT = "blockdata"

  def read(nbt: NBTTagCompound): BlockData = {
    if (!nbt.hasKey(BlockData_NBT)) Invalid
    else {
      val compound = nbt.getCompound(BlockData_NBT)
      new BlockData(compound.getString(Name_NBT))
    }
  }

  val Invalid: BlockData = new BlockData("Unknown:Dummy") {
    override def equals(o: Any) = false

    override def hashCode = 0

    override def write(nbt: NBTTagCompound): NBTTagCompound = nbt

    override def toString = "BlockData@Invaild"

    override def getLocalizedName = "Unknown:Dummy"
  }

  val comparator: Comparator[BlockData] = Ordering.by((b: BlockData) => b.name)
}

case class BlockData(name: ResourceLocation) extends Ordered[BlockData] {

  def this(resourceName: String) {
    this(new ResourceLocation(resourceName))
  }

  def this(state: IBlockState) {
    this(ForgeRegistries.BLOCKS.getKey(state.getBlock))
  }

  override def equals(o: Any): Boolean = {
    o match {
      case data: BlockData =>
        name == data.name
    }
  }

  override def hashCode: Int = this.name.hashCode

  def write(nbt: NBTTagCompound): NBTTagCompound = {
    val compound = new NBTTagCompound
    compound.setString(BlockData.Name_NBT, name.toString)
    nbt.setTag(BlockData.BlockData_NBT, compound)
    nbt
  }

  override def toString: String = name.toString

  def getLocalizedName: String = {
    val sb = new StringBuilder
    sb.append(name)
    sb.append("  ").append(Option(ForgeRegistries.BLOCKS.getValue(name)).fold("")(_.getTranslationKey))
    sb.toString
  }

  override def compare(that: BlockData): Int = BlockData.comparator.compare(this, that)
}
