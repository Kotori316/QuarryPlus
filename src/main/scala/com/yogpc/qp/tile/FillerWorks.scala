package com.yogpc.qp.tile

import com.yogpc.qp.{PowerManager, QuarryPlus}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.util.{EnumFacing, EnumHand}
import net.minecraft.world.WorldServer
import net.minecraftforge.common.ForgeHooks

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

  def area: Option[(BlockPos, BlockPos)] = None
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
    "FillAll" -> (tag => new FillAll(BlockPos.fromLong(tag.getLong("minPos")), BlockPos.fromLong(tag.getLong("maxPos")))),
    "FillBox" -> (tag => new FillBox(BlockPos.fromLong(tag.getLong("minPos")), BlockPos.fromLong(tag.getLong("maxPos"))))
  )

  object Wait extends FillerWorks {
    override val working = false
    override val name = "Wait"

    override def tick(tile: TileFiller): Unit = ()

    override def next(tile: TileFiller): FillerWorks = this

    protected override def save(tag: NBTTagCompound): NBTTagCompound = tag
  }

  abstract class Fill(val min: BlockPos, val max: BlockPos) extends FillerWorks {
    override val area = Option(min, max)

    override def toString = super.toString + s" min=$min, max=$max"

    override def tick(tile: TileFiller): Unit = {
      val fakePlayer = QuarryFakePlayer.get(tile.getWorld.asInstanceOf[WorldServer], tile.getPos)
      for {
        (stack, block) <- tile.inventory.firstBlock()
        targetPos <- targetIterator
          .find(p => tile.getWorkingWorld.mayPlace(block, p, false, EnumFacing.UP, fakePlayer))
      } {
        if (PowerManager.useEnergyFillerWork(tile, true)) {
          PowerManager.useEnergyFillerWork(tile, false)
          fakePlayer.setHeldItem(EnumHand.MAIN_HAND, stack)
          ForgeHooks.onPlaceItemIntoWorld(stack, fakePlayer, tile.getWorkingWorld, targetPos, EnumFacing.UP, 0.5f, 1f, 0.5f, EnumHand.MAIN_HAND)
          tile.inventory.markDirty()
        }
      }
    }

    protected def targetIterator: Iterator[BlockPos.MutableBlockPos]

    override def next(tile: TileFiller): FillerWorks = {
      tile.inventory.firstBlock() match {
        case Some((_, b)) =>
          if (targetIterator
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

  class FillAll(m: BlockPos, M: BlockPos) extends Fill(m, M) {
    override val name = "FillAll"

    protected def targetIterator: Iterator[BlockPos.MutableBlockPos] = BlockPos.getAllInBoxMutable(min, max).asScala.iterator
  }

  class FillBox(m: BlockPos, M: BlockPos) extends Fill(m, M) {
    override val name = "FillBox"

    override protected def targetIterator: Iterator[BlockPos.MutableBlockPos] = {
      val mutable = new BlockPos.MutableBlockPos()
      val downUp = for {z <- Range.inclusive(min.getZ, max.getZ); x <- Range.inclusive(min.getX, max.getX)} yield (x, z)
      val middle = downUp.filter { case (x, z) => (x == min.getX || x == max.getX) || (z == min.getZ || z == max.getZ) }
      Iterator.range(min.getY, max.getY + 1).flatMap { y =>
        if (y == min.getY || y == max.getY)
          downUp.iterator.map { case (x, z) => mutable.setPos(x, y, z); mutable }
        else
          middle.iterator.map { case (x, z) => mutable.setPos(x, y, z); mutable }
      }
    }
  }

}
