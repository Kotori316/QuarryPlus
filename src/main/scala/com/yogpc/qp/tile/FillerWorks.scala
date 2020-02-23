package com.yogpc.qp.tile

import com.yogpc.qp.QuarryPlus
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.BlockPos
import net.minecraft.world.WorldServer

import scala.collection.JavaConverters._

trait FillerWorks {
  val name: String
  val working = true

  override def toString = name

  /**
    * Must be called in server world.
    */
  def tick(tile: TileFiller): Unit

  /**
    * Called every tick. Return itself if work hasn't done.
    */
  def next(tile: TileFiller): FillerWorks

  final def toNBT: NBTTagCompound = {
    val tag = new NBTTagCompound
    tag.setString("name", this.name)
    save(tag)
  }

  protected def save(tag: NBTTagCompound): NBTTagCompound
}

object FillerWorks {
  def fromNBT(tag: NBTTagCompound): FillerWorks = {
    val name = tag.getString("name")
    restoreMap.get(name).map(_.apply(tag)).getOrElse {
      QuarryPlus.LOGGER.error(s"Unregistered key $name was used to read work $tag.")
      Wait
    }
  }

  private[this] final val restoreMap: Map[String, NBTTagCompound => FillerWorks] = Map(
    Wait.name -> (_ => Wait),
    "FillAll" -> (tag => new FillAll(BlockPos.fromLong(tag.getLong("min")), BlockPos.fromLong(tag.getLong("max"))))
  )

  object Wait extends FillerWorks {
    override val working = false
    override val name = "Wait"

    override def tick(tile: TileFiller): Unit = ()

    override def next(tile: TileFiller): FillerWorks = this

    protected override def save(tag: NBTTagCompound): NBTTagCompound = tag
  }

  class FillAll(val min: BlockPos, val max: BlockPos) extends FillerWorks {
    override val name = "FillAll"

    override def toString = super.toString + s" min=$min, max=$max"

    override def tick(tile: TileFiller): Unit = {
      val fakePlayer = QuarryFakePlayer.get(tile.getWorld.asInstanceOf[WorldServer], tile.getPos)
      for {
        (stack, block) <- tile.inventory.firstBlock()
        targetPos <- BlockPos.getAllInBoxMutable(min, max).asScala
          .find(p => tile.getWorkingWorld.mayPlace(block, p, false, EnumFacing.UP, fakePlayer))
      } {
        val e = TileFiller.power * APowerTile.MJToMicroMJ / 100
        if (tile.useEnergy(e, e, false, EnergyUsage.FILLER) == e) {
          tile.useEnergy(e, e, true, EnergyUsage.FILLER)
          tile.getWorkingWorld.setBlockState(targetPos, block.getDefaultState)
          stack.shrink(1)
          tile.inventory.markDirty()
        }
      }
    }

    override def next(tile: TileFiller): FillerWorks = {
      tile.inventory.firstBlock() match {
        case Some((_, b)) =>
          if (BlockPos.getAllInBoxMutable(min, max).asScala
            .forall(p => !tile.getWorkingWorld.mayPlace(b, p, false, EnumFacing.UP, null))) {
            Wait // Work has been done.
          } else {
            this // Working
          }
        case None => this
      }
    }

    override protected def save(tag: NBTTagCompound): NBTTagCompound = {
      tag.setLong("minPos", min.toLong)
      tag.setLong("maxPos", max.toLong)
      tag
    }
  }

}