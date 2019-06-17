package com.yogpc.qp.machines.quarry

import com.yogpc.qp.NBTWrapper
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.math.BlockPos

trait QuarryAction {

  def action(target: BlockPos): Unit

  def next(): BlockPos

  def mode: TileQuarry2.Mode

  def write(nbt: NBTTagCompound): NBTTagCompound
}

object QuarryAction {
  private[this] final val mode_nbt = "mode"
  val none: QuarryAction = new QuarryAction {
    override def action(target: BlockPos): Unit = ()

    override def next(): BlockPos = BlockPos.ORIGIN

    override val mode: TileQuarry2.Mode = TileQuarry2.none

    override def write(nbt: NBTTagCompound) = {
      nbt.put(mode_nbt, mode.toNBT)
      nbt
    }
  }
  val wating: QuarryAction = new QuarryAction {
    override def action(target: BlockPos): Unit = ()

    override def next(): BlockPos = BlockPos.ORIGIN

    override val mode: TileQuarry2.Mode = TileQuarry2.waiting

    override def write(nbt: NBTTagCompound): NBTTagCompound = {
      nbt.put(mode_nbt, mode.toNBT)
      nbt
    }
  }

  def load(tag: NBTTagCompound, name: String): QuarryAction = {
    val nbt = tag.getCompound(name)
    val mode = nbt.getString(mode_nbt)
    mode match {
      case none.mode.toString => none
      case wating.mode.toString => wating
      case  => none
    }
  }

  implicit val actionToNbt: QuarryAction NBTWrapper NBTTagCompound = action => action.write(new NBTTagCompound)

}
