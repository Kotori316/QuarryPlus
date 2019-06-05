package com.yogpc.qp.machines.workbench

import java.util.Comparator

import com.yogpc.qp._
import net.minecraft.block.state.IBlockState
import net.minecraft.client.resources.I18n
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.ResourceLocation
import net.minecraftforge.api.distmarker.{Dist, OnlyIn}
import net.minecraftforge.registries.ForgeRegistries

object BlockData {
  final val Name_NBT = "name"
  //  final val Meta_NBT = "meta"
  final val BlockData_NBT = "blockdata"

  def read(nbt: NBTTagCompound): BlockData = {
    new BlockData(nbt.getString(Name_NBT))
  }

  val Invalid: BlockData = new BlockData("unknown:dummy") {
    override def equals(o: Any) = false

    override def hashCode = 0

    override def toString = "BlockData@Invaild"

    override def getLocalizedName = "Unknown:Dummy"
  }

  val comparator: Comparator[BlockData] = Ordering.by((b: BlockData) => b.name)

  implicit val dataToNbt: NBTWrapper[BlockData, NBTTagCompound] = data => {
    val nbt = new NBTTagCompound
    nbt.putString(BlockData.Name_NBT, data.name.toString)
    nbt
  }
}

case class BlockData(name: ResourceLocation) extends Ordered[BlockData] {

  def this(resourceName: String) {
    this(new ResourceLocation(resourceName))
  }

  def this(state: IBlockState) {
    this(ForgeRegistries.BLOCKS.getKey(state.getBlock))
  }

  override def toString: String = name.toString

  @OnlyIn(Dist.CLIENT)
  def getLocalizedName: String = {
//    val sb = new StringBuilder
//    sb.append(name)
//    sb.append("  ").append(Option(ForgeRegistries.BLOCKS.getValue(name)).map(_.getTranslationKey).fold("")(s => I18n.format(s)))
//    sb.toString
    Option(ForgeRegistries.BLOCKS.getValue(name)).map(_.getTranslationKey).fold("")(s => I18n.format(s))
  }

  override def compare(that: BlockData): Int = BlockData.comparator.compare(this, that)
}
