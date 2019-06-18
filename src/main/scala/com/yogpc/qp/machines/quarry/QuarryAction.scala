package com.yogpc.qp.machines.quarry

import com.yogpc.qp.NBTWrapper
import com.yogpc.qp.machines.PowerManager
import com.yogpc.qp.utils.Holder
import net.minecraft.block.state.IBlockState
import net.minecraft.nbt.{NBTDynamicOps, NBTTagCompound, NBTTagList}
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.common.util.Constants.NBT

import scala.collection.JavaConverters

trait QuarryAction {

  def action(target: BlockPos): Unit

  def nextTarget(): BlockPos

  def nextAction(quarry2: TileQuarry2): QuarryAction

  def canGoNext(quarry: TileQuarry2): Boolean

  def mode: TileQuarry2.Mode

  def write(nbt: NBTTagCompound): NBTTagCompound
}

object QuarryAction {
  private[this] final val mode_nbt = "mode"
  val none: QuarryAction = new QuarryAction {
    override def action(target: BlockPos): Unit = ()

    override def nextTarget(): BlockPos = BlockPos.ORIGIN

    override val mode: TileQuarry2.Mode = TileQuarry2.none

    override def write(nbt: NBTTagCompound) = {
      nbt.put(mode_nbt, mode.toNBT)
      nbt
    }

    override def nextAction(quarry2: TileQuarry2): QuarryAction = this

    override def canGoNext(quarry: TileQuarry2): Boolean = false
  }
  val waiting: QuarryAction = new QuarryAction {
    override def action(target: BlockPos): Unit = ()

    override def nextTarget(): BlockPos = BlockPos.ORIGIN

    override val mode: TileQuarry2.Mode = TileQuarry2.waiting

    override def write(nbt: NBTTagCompound): NBTTagCompound = {
      nbt.put(mode_nbt, mode.toNBT)
      nbt
    }

    override def nextAction(quarry2: TileQuarry2): QuarryAction = new MakeFrame(quarry2)

    override def canGoNext(quarry: TileQuarry2): Boolean = quarry.getStoredEnergy * 3 > quarry.getMaxStored
  }

  class MakeFrame(quarry2: TileQuarry2) extends QuarryAction {

    var frameTargets: List[BlockPos] = {
      val r = quarry2.area

      def a(y: Int) = {
        val firstZ = near(quarry2.getPos.getZ, r.zMin, r.zMax)
        val lastZ = far(quarry2.getPos.getZ, r.zMin, r.zMax)
        Range(r.xMin, r.xMax).map(x => new BlockPos(x, y, firstZ)) ++
          Range(firstZ, lastZ, (lastZ - firstZ).signum).map(z => new BlockPos(r.xMax, y, z)) ++
          Range(r.xMax, r.xMin, -1).map(x => new BlockPos(x, y, lastZ)) ++
          Range(lastZ, firstZ, (firstZ - lastZ).signum).map(z => new BlockPos(r.xMin, y, z))
      }.toList

      def b(y: Int) = {
        for (x <- List(r.xMin, r.xMax);
             z <- List(r.zMin, r.zMax)) yield new BlockPos(x, y, z)
      }

      a(r.yMin) ++ Range.inclusive(r.yMin + 1, r.yMax - 1).flatMap(b) ++ a(r.yMax)
    }

    override def action(target: BlockPos): Unit = {
      frameTargets match {
        case head :: til if head == target =>
          if (checkPlacable(quarry2.getWorld, target, Holder.blockFrame.getDammingState)
            || quarry2.breakBlock(quarry2.getWorld, target)) {
            if (PowerManager.useEnergyFrameBuild(quarry2, quarry2.enchantments.unbreaking)) {
              quarry2.getWorld.setBlockState(target, Holder.blockFrame.getDammingState)
              frameTargets = til
            }
          }
        case _ =>
      }
    }

    override def nextTarget(): BlockPos = frameTargets.headOption.getOrElse(BlockPos.ORIGIN)

    override def nextAction(quarry2: TileQuarry2): QuarryAction = none

    override def mode: TileQuarry2.Mode = TileQuarry2.buildFrame

    override def write(nbt: NBTTagCompound): NBTTagCompound = {
      import com.yogpc.qp._
      val list = frameTargets.map(_.toLong.toNBT).foldLeft(new NBTTagList) { case (l, tag) => l.add(tag); l }
      nbt.put("list", list)
      nbt
    }

    override def canGoNext(quarry: TileQuarry2): Boolean = frameTargets.isEmpty
  }

  def near[A](pos: A, x1: A, x2: A)(implicit proxy: Numeric[A]): A = {
    val c = (proxy.minus _).curried(pos) andThen proxy.abs
    List(x1, x2).minBy(c)
  }

  def far[A](pos: A, x1: A, x2: A)(implicit proxy: Numeric[A]): A = {
    val c = (proxy.minus _).curried(pos) andThen proxy.abs
    List(x1, x2).maxBy(c)
  }

  def checkPlacable(world: World, pos: BlockPos, toPlace: IBlockState): Boolean = {
    val state = world.getBlockState(pos)
    state.isAir(world, pos) || state == toPlace
  }

  def load(quarry: TileQuarry2, tag: NBTTagCompound, name: String): QuarryAction = {
    val nbt = tag.getCompound(name)
    val mode = nbt.getString(mode_nbt)
    mode match {
      case TileQuarry2.none.toString => none
      case TileQuarry2.waiting.toString => waiting
      case TileQuarry2.buildFrame.toString =>
        import com.yogpc.qp._
        val task = new MakeFrame(quarry)
        task.frameTargets = JavaConverters.asScalaBuffer(nbt.getList("list", NBT.TAG_LONG))
          .flatMap(NBTDynamicOps.INSTANCE.getNumberValue(_).asScala.map(_.longValue()))
          .map(BlockPos.fromLong).toList
        task
      case _ => none
    }
  }

  implicit val actionToNbt: QuarryAction NBTWrapper NBTTagCompound = action => action.write(new NBTTagCompound)

}
