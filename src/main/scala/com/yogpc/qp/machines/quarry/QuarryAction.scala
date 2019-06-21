package com.yogpc.qp.machines.quarry

import com.yogpc.qp._
import com.yogpc.qp.machines.PowerManager
import com.yogpc.qp.machines.base.IModule
import com.yogpc.qp.machines.pump.TilePump
import com.yogpc.qp.packet.PacketHandler
import com.yogpc.qp.packet.quarry2.ActionMessage
import com.yogpc.qp.utils.Holder
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.nbt.{NBTDynamicOps, NBTTagCompound, NBTTagList}
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraftforge.common.util.Constants.NBT
import org.apache.logging.log4j.MarkerManager

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
  final val MARKER = MarkerManager.getMarker("QUARRY_ACTION")
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
      val firstZ = near(quarry2.getPos.getZ, r.zMin, r.zMax)
      val lastZ = far(quarry2.getPos.getZ, r.zMin, r.zMax)
      QuarryPlus.LOGGER.debug(MARKER, s"Make targets list of building frame. $r, firstZ=$firstZ, lastZ=$lastZ")

      def a(y: Int) = {
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
          if (!checkBreakable(quarry2.getWorld, target, quarry2.getWorld.getBlockState(target), quarry2.modules)) {
            frameTargets = til
          } else {
            if (checkPlaceable(quarry2.getWorld, target, Holder.blockFrame.getDammingState)
              || quarry2.breakBlock(quarry2.getWorld, target)) {
              if (PowerManager.useEnergyFrameBuild(quarry2, quarry2.enchantments.unbreaking)) {
                quarry2.getWorld.setBlockState(target, Holder.blockFrame.getDammingState)
                frameTargets = til
              }
            }
          }
        case _ =>
      }
    }

    override def nextTarget(): BlockPos = frameTargets.headOption.getOrElse(BlockPos.ORIGIN)

    override def nextAction(quarry2: TileQuarry2): QuarryAction = new BreakBlock(quarry2, quarry2.area.yMin - 1)

    override def mode: TileQuarry2.Mode = TileQuarry2.buildFrame

    override def write(nbt: NBTTagCompound): NBTTagCompound = {
      nbt.put(mode_nbt, mode.toNBT)
      val list = frameTargets.map(_.toLong.toNBT).foldLeft(new NBTTagList) { case (l, tag) => l.add(tag); l }
      nbt.put("list", list)
      nbt
    }

    def read(nbt: NBTTagCompound): MakeFrame = {
      frameTargets = JavaConverters.asScalaBuffer(nbt.getList("list", NBT.TAG_LONG))
        .flatMap(NBTDynamicOps.INSTANCE.getNumberValue(_).asScala.map(_.longValue()))
        .map(BlockPos.fromLong).toList
      this
    }

    override def canGoNext(quarry: TileQuarry2): Boolean = frameTargets.isEmpty
  }

  class BreakBlock(quarry2: TileQuarry2, y: Int) extends QuarryAction {
    var digTargets: List[BlockPos] = QuarryAction.digTargets(quarry2.area, quarry2.getPos, y)

    var headX: Double = (quarry2.area.xMin + quarry2.area.xMax + 1) / 2
    var headY: Double = y + 1
    var headZ: Double = (quarry2.area.zMin + quarry2.area.zMax + 1) / 2
    var movingHead = false

    override def action(target: BlockPos): Unit = {
      if (movingHead) {
        val x = target.getX - this.headX
        val y = target.getY + 1 - this.headY
        val z = target.getZ - this.headZ
        val distance = Math.sqrt(x * x + y * y + z * z)
        val blocks = PowerManager.useEnergyQuarryHead(quarry2, distance, quarry2.enchantments.unbreaking)
        if (blocks * 2 >= distance) {
          this.headX = target.getX
          this.headY = target.getY + 1
          this.headZ = target.getZ
          movingHead = false
          if (!quarry2.getWorld.isRemote)
            PacketHandler.sendToClient(ActionMessage.create(quarry2), quarry2.getWorld)
        } else {
          if (blocks > 0) {
            this.headX += x * blocks / distance
            this.headY += y * blocks / distance
            this.headZ += z * blocks / distance
            if (!quarry2.getWorld.isRemote)
              PacketHandler.sendToClient(ActionMessage.create(quarry2), quarry2.getWorld)
          }
          movingHead = true
        }
      }
      if (quarry2.breakBlock(quarry2.getWorld, target)) {
        quarry2.getWorld.setBlockState(target, Blocks.AIR.getDefaultState)
        digTargets = digTargets.tail.dropWhile(p => !checkBreakable(quarry2.getWorld, p, quarry2.getWorld.getBlockState(p), quarry2.modules))
      }
    }

    override def nextTarget() = digTargets.headOption.getOrElse(BlockPos.ORIGIN)

    override def nextAction(quarry2: TileQuarry2) = if (y > 1) new BreakBlock(quarry2, y - 1) else none

    override def canGoNext(quarry: TileQuarry2) = digTargets.isEmpty

    override def mode = TileQuarry2.breakBlock

    override def write(nbt: NBTTagCompound) = {
      nbt.put(mode_nbt, mode.toNBT)
      val list = digTargets.map(_.toLong.toNBT).foldLeft(new NBTTagList) { case (l, tag) => l.add(tag); l }
      nbt.put("list", list)
      nbt.putInt("y", y)
      nbt.putDouble("headX", headX)
      nbt.putDouble("headY", headY)
      nbt.putDouble("headZ", headZ)
      nbt
    }

    def read(nbt: NBTTagCompound): BreakBlock = {
      this.digTargets = JavaConverters.asScalaBuffer(nbt.getList("list", NBT.TAG_LONG))
        .flatMap(NBTDynamicOps.INSTANCE.getNumberValue(_).asScala.map(_.longValue()))
        .map(BlockPos.fromLong).toList
      this.headX = nbt.getDouble("headX")
      this.headY = nbt.getDouble("headY")
      this.headZ = nbt.getDouble("headZ")
      this
    }
  }

  def digTargets(r: TileQuarry2.Area, pos: BlockPos, y: Int, log: Boolean = true) = {
    val firstZ = near(pos.getZ, r.zMin + 1, r.zMax - 1)
    val lastZ = far(pos.getZ, r.zMin + 1, r.zMax - 1)
    if (log) QuarryPlus.LOGGER.debug(MARKER, s"Make targets list of breaking blocks. $r, firstZ=$firstZ, lastZ=$lastZ")
    Range.inclusive(r.xMin + 1, r.xMax - 1)
      .map(x => Range.inclusive(firstZ, lastZ, (lastZ - firstZ).signum).map(z => new BlockPos(x, y, z)))
      .zip(Stream.iterate(true)(b => !b))
      .flatMap {
        case (p1, true) => p1
        case (p2, false) => p2.reverse
      }.toList
  }

  def near[A](pos: A, x1: A, x2: A)(implicit proxy: Numeric[A]): A = {
    val c = (proxy.minus _).curried(pos) andThen proxy.abs
    List(x1, x2).reduceLeft[A] { case (b, a) => if (proxy.lt(c(a), c(b))) a else b }
  }

  def far[A](pos: A, x1: A, x2: A)(implicit proxy: Numeric[A]): A = {
    val c = (proxy.minus _).curried(pos) andThen proxy.abs
    List(x1, x2).reduceRight[A] { case (a, b) => if (proxy.gt(c(a), c(b))) a else b }
  }

  def checkPlaceable(world: World, pos: BlockPos, toPlace: IBlockState): Boolean = {
    val state = world.getBlockState(pos)
    state.isAir(world, pos) || state == toPlace
  }

  def checkBreakable(world: World, pos: BlockPos, state: IBlockState, modules: Seq[IModule]): Boolean = {
    !state.isAir(world, pos) &&
      state.getBlockHardness(world, pos) != -1 &&
      !state.getBlockHardness(world, pos).isInfinity &&
      (!TilePump.isLiquid(state) || modules.exists(m => m.isInstanceOf[TilePump]))
  }

  val getNamed: (NBTTagCompound, String) => NBTTagCompound = _.getCompound(_)
  val loadFromNBT: NBTTagCompound => TileQuarry2 => QuarryAction = nbt => quarry => {
    val mode = nbt.getString(mode_nbt)
    val action = mode match {
      case TileQuarry2.none.toString => none
      case TileQuarry2.waiting.toString => waiting
      case TileQuarry2.buildFrame.toString => new MakeFrame(quarry)
      case TileQuarry2.breakBlock.toString => new BreakBlock(quarry, nbt.getInt("y"))
      case _ => none
    }
    if (quarry.hasWorld && quarry.getWorld.isRemote) {
      action
    } else {
      action match {
        case QuarryAction.none | QuarryAction.waiting => action
        case makeFrame: MakeFrame =>
          makeFrame.read(nbt)
        case breakBlock: BreakBlock =>
          breakBlock.read(nbt)
        case _ => none
      }
    }
  }
  val load: (TileQuarry2, NBTTagCompound, String) => QuarryAction = {
    case (q, t, s) => loadFromNBT(getNamed(t, s))(q)
  }


  implicit val actionToNbt: QuarryAction NBTWrapper NBTTagCompound = action => action.write(new NBTTagCompound)

}
