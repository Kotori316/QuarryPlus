package com.yogpc.qp.machines.workbench

import java.util.Comparator

import com.yogpc.qp._
import net.minecraft.block.BlockState
import net.minecraft.nbt.CompoundNBT
import net.minecraft.util.ResourceLocation
import net.minecraft.util.text.{ITextComponent, StringTextComponent}
import net.minecraftforge.api.distmarker.{Dist, OnlyIn}
import net.minecraftforge.registries.ForgeRegistries

object BlockData {
  final val Name_NBT = "name"
  //  final val Meta_NBT = "meta"
  final val BlockData_NBT = "blockdata"

  def read(nbt: CompoundNBT): BlockData = {
    new BlockData(nbt.getString(Name_NBT))
  }

  val Invalid: BlockData = new BlockData("unknown:dummy") {
    override def equals(o: Any) = false

    override def hashCode = 0

    override def toString = "BlockData@Invaild"

    override def getDisplayText = new StringTextComponent("Unknown:Dummy")
  }

  val comparator: Comparator[BlockData] = Ordering.by((b: BlockData) => b.name)

  implicit val dataToNbt: NBTWrapper[BlockData, CompoundNBT] = data => {
    val nbt = new CompoundNBT
    nbt.putString(BlockData.Name_NBT, data.name.toString)
    nbt
  }
}

case class BlockData(name: ResourceLocation) extends Ordered[BlockData] {

  def this(resourceName: String) {
    this(new ResourceLocation(resourceName))
  }

  def this(state: BlockState) {
    this(ForgeRegistries.BLOCKS.getKey(state.getBlock))
  }

  override def toString: String = name.toString

  @OnlyIn(Dist.CLIENT)
  def getDisplayText: ITextComponent = {
    //    val sb = new StringBuilder
    //    sb.append(name)
    //    sb.append("  ").append(Option(ForgeRegistries.BLOCKS.getValue(name)).map(_.getTranslationKey).fold("")(s => I18n.format(s)))
    //    sb.toString
    Option(ForgeRegistries.BLOCKS.getValue(name)).map(_.getNameTextComponent).getOrElse(new StringTextComponent(name.toString))
  }

  override def compare(that: BlockData): Int = BlockData.comparator.compare(this, that)
}
