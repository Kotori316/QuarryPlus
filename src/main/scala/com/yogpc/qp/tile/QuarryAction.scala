package com.yogpc.qp.tile

import com.yogpc.qp._
import com.yogpc.qp.packet.PacketHandler
import com.yogpc.qp.packet.quarry2.ActionMessage
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.SoundCategory
import net.minecraft.util.math.{AxisAlignedBB, BlockPos}
import net.minecraft.world.World
import org.apache.logging.log4j.MarkerManager

trait QuarryAction {

  def action(target: BlockPos): Unit

  def nextTarget(): BlockPos

  def nextAction(quarry2: TileQuarry2): QuarryAction

  def canGoNext(quarry: TileQuarry2): Boolean

  def mode: TileQuarry2.Mode

  def serverWrite(nbt: NBTTagCompound): NBTTagCompound = clientWrite(nbt)

  def clientWrite(nbt: NBTTagCompound): NBTTagCompound = {
    nbt.setTag(QuarryAction.mode_nbt, mode.toNBT)
    nbt
  }
}

object QuarryAction {
  final val MARKER = MarkerManager.getMarker("QUARRY_ACTION")
  private final val mode_nbt = "mode"
  val none: QuarryAction = new QuarryAction {
    override def action(target: BlockPos): Unit = ()

    override def nextTarget(): BlockPos = BlockPos.ORIGIN

    override val mode: TileQuarry2.Mode = TileQuarry2.none

    override def nextAction(quarry2: TileQuarry2): QuarryAction = this

    override def canGoNext(quarry: TileQuarry2): Boolean = false
  }
  val waiting: QuarryAction = new QuarryAction {
    override def action(target: BlockPos): Unit = ()

    override def nextTarget(): BlockPos = BlockPos.ORIGIN

    override val mode: TileQuarry2.Mode = TileQuarry2.waiting

    override def nextAction(quarry2: TileQuarry2): QuarryAction = new BreakInsideFrame(quarry2)

    override def canGoNext(quarry: TileQuarry2): Boolean = quarry.getStoredEnergy * 3 > quarry.getMaxStored
  }

  class MakeFrame(quarry2: TileQuarry2) extends QuarryAction {

    var frameTargets: List[BlockPos] = makeList

    def makeList = if (quarry2.hasWorld && !quarry2.getWorld.isRemote) {
      val r = quarry2.area
      val firstZ = near(quarry2.getPos.getZ, r.zMin, r.zMax)
      val lastZ = far(quarry2.getPos.getZ, r.zMin, r.zMax)
      QuarryPlus.LOGGER.debug(MARKER, s"Making targets list of building frame. $r, firstZ=$firstZ, lastZ=$lastZ")

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
    } else {
      Nil
    }

    override def action(target: BlockPos): Unit = {
      frameTargets match {
        case head :: til if head == target =>
          val state = quarry2.getWorld.getBlockState(target)
          if (checkPlaceable(quarry2.getWorld, target, state, QuarryPlusI.blockFrame.getDammingState)) {
            if (PowerManager.useEnergyFrameBuild(quarry2, quarry2.enchantments.unbreaking)) {
              quarry2.getWorld.setBlockState(target, QuarryPlusI.blockFrame.getDammingState)
              frameTargets = til
            }
          } else {
            if (!checkBreakable(quarry2.getWorld, target, state, quarry2.modules)) {
              frameTargets = til
            } else if (quarry2.breakBlock(quarry2.getWorld, target, state)) {
              if (PowerManager.useEnergyFrameBuild(quarry2, quarry2.enchantments.unbreaking)) {
                quarry2.getWorld.setBlockState(target, QuarryPlusI.blockFrame.getDammingState)
                frameTargets = til
              }
            }
          }
        case _ =>
      }
    }

    override def nextTarget(): BlockPos = frameTargets.dropWhile(p => quarry2.getWorld.getBlockState(p).getBlock == QuarryPlusI.blockFrame).headOption.getOrElse(BlockPos.ORIGIN)

    override def nextAction(quarry2: TileQuarry2): QuarryAction = new BreakBlock(quarry2, quarry2.area.yMin - 1, quarry2.getPos)

    override def mode: TileQuarry2.Mode = TileQuarry2.buildFrame

    override def serverWrite(nbt: NBTTagCompound): NBTTagCompound = {
      //      val list = frameTargets.map(_.toLong.toNBT).foldLeft(new NBTTagList) { case (l, tag) => l.appendTag(tag); l }
      //      nbt.setTag("list", list)
      super.serverWrite(nbt)
    }

    def read(nbt: NBTTagCompound): MakeFrame = {
      //      frameTargets = nbt.getTagList("list", NBT.TAG_LONG).iterator().asScala
      //        .collect { case num: NBTPrimitive => num.getLong }
      //        .map(BlockPos.fromLong).toList
      this
    }

    override def canGoNext(quarry: TileQuarry2): Boolean = frameTargets.isEmpty
  }

  class BreakInsideFrame(quarry2: TileQuarry2) extends QuarryAction {
    var insideFrame: List[BlockPos] = if (quarry2.hasWorld && !quarry2.getWorld.isRemote) {
      insideFrameArea(quarry2.area)
        .dropWhile(p => !checkBreakable(quarry2.getWorld, p, quarry2.getWorld.getBlockState(p), quarry2.modules))
    } else {
      Nil
    }

    override def action(target: BlockPos): Unit = {
      insideFrame match {
        case head :: tl if head == target =>
          val state = quarry2.getWorld.getBlockState(target)
          if (checkBreakable(quarry2.getWorld, head, state, quarry2.modules)) {
            if (quarry2.breakBlock(quarry2.getWorld, head, state)) {
              quarry2.getWorld.setBlockState(target, Blocks.AIR.getDefaultState)
              playSound(state, quarry2.getWorld, head)
              insideFrame = tl.dropWhile(p => !checkBreakable(quarry2.getWorld, p, quarry2.getWorld.getBlockState(p), quarry2.modules))
            }
          } else {
            insideFrame = insideFrame.dropWhile(p => !checkBreakable(quarry2.getWorld, p, quarry2.getWorld.getBlockState(p), quarry2.modules))
          }
        case _ =>
      }
    }

    override def nextTarget() = insideFrame.headOption.getOrElse(BlockPos.ORIGIN)

    override def nextAction(quarry2: TileQuarry2) = if (!quarry2.frameMode) new MakeFrame(quarry2) else none

    override def canGoNext(quarry: TileQuarry2) = insideFrame.isEmpty

    override def mode = TileQuarry2.breakInsideFrame

    override def serverWrite(nbt: NBTTagCompound) = {
      //      val list = insideFrame.map(_.toLong.toNBT).foldLeft(new NBTTagList) { case (l, tag) => l.appendTag(tag); l }
      //      nbt.setTag("list", list)
      super.serverWrite(nbt)
    }

    def read(nbt: NBTTagCompound): BreakInsideFrame = {
      //      insideFrame = nbt.getTagList("list", NBT.TAG_LONG).iterator().asScala
      //        .collect { case num: NBTPrimitive => num.getLong }
      //        .map(BlockPos.fromLong).toList
      this
    }
  }

  class BreakBlock(quarry2: TileQuarry2, y: Int, targetBefore: BlockPos, var headX: Double, var headY: Double, var headZ: Double) extends QuarryAction {

    def this(quarry2: TileQuarry2, y: Int, targetBefore: BlockPos) {
      this(quarry2, y, targetBefore, (quarry2.area.xMin + quarry2.area.xMax + 1) / 2, y + 1, (quarry2.area.zMin + quarry2.area.zMax + 1) / 2)
    }

    var digTargets: List[BlockPos] = (if (quarry2.hasWorld && !quarry2.getWorld.isRemote) QuarryAction.digTargets(quarry2.area, targetBefore, y) else Nil)
      .dropWhile(p => !checkBreakable(quarry2.getWorld, p, quarry2.getWorld.getBlockState(p), quarry2.modules))

    var movingHead = true

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
            PacketHandler.sendToDimension(ActionMessage.create(quarry2), quarry2.getWorld.provider.getDimension)
        } else {
          if (blocks > 0) {
            this.headX += x * blocks / distance
            this.headY += y * blocks / distance
            this.headZ += z * blocks / distance
            if (!quarry2.getWorld.isRemote)
              PacketHandler.sendToAround(ActionMessage.create(quarry2), quarry2.getWorld, quarry2.getPos)
          }
          movingHead = true
        }
      }
      if (!movingHead) {
        digTargets match {
          case Nil =>
            val poses = QuarryAction.digTargets(quarry2.area, targetBefore, y, log = false).dropWhile(p => !checkBreakable(quarry2.getWorld, p, quarry2.getWorld.getBlockState(p), quarry2.modules))
            if (poses.nonEmpty) {
              digTargets = poses
            }
          case head :: tl if target == head =>
            val state = quarry2.getWorld.getBlockState(target)
            if (quarry2.breakBlock(quarry2.getWorld, target, state)) {
              // Replacer works for non liquid block.
              if (!TilePump.isLiquid(state) && !state.getBlock.isAir(state, quarry2.getWorld, target)) {
                val replaced = quarry2.modules.foldLeft(IModule.NoAction: IModule.Result) { case (r, m) => IModule.Result.combine(r, m.invoke(IModule.AfterBreak(quarry2.getWorld, target, state))) }
                if (!replaced.done) { // Not replaced
                  quarry2.getWorld.setBlockState(target, Blocks.AIR.getDefaultState)
                  playSound(state, quarry2.getWorld, target)
                }
              } else {
                quarry2.getWorld.setBlockState(target, Blocks.AIR.getDefaultState)
                playSound(state, quarry2.getWorld, target)
              }
              digTargets = tl.dropWhile(p => !checkBreakable(quarry2.getWorld, p, quarry2.getWorld.getBlockState(p), quarry2.modules))
              movingHead = true
            }
        }
      }
    }

    override def nextTarget() = digTargets.headOption.getOrElse(new BlockPos(quarry2.area.xMin + 1, y, quarry2.area.zMin + 1))

    override def nextAction(quarry2: TileQuarry2) =
      if (y > quarry2.yLevel) new BreakBlock(quarry2, y - 1, quarry2.target, headX, headY, headZ) else new CheckDrops(quarry2, quarry2.yLevel)

    override def canGoNext(quarry: TileQuarry2): Boolean = {
      if (digTargets.isEmpty) {
        val set = quarry.modules.flatMap(IModule.replaceBlocks(y)).toSet
        val list = QuarryAction.digTargets(quarry2.area, targetBefore, y, log = false)
          .filter(p => checkBreakable(quarry2.getWorld, p, quarry2.getWorld.getBlockState(p), quarry2.modules))
        list.forall(quarry.getWorld.getBlockState _ andThen set)
      } else {
        false
      }
    }

    override def mode = TileQuarry2.breakBlock

    override def serverWrite(nbt: NBTTagCompound) = {
      //      val list = digTargets.map(_.toLong.toNBT).foldLeft(new NBTTagList) { case (l, tag) => l.appendTag(tag); l }
      //      nbt.setTag("list", list)
      nbt.setInteger("y", y)
      nbt.setLong("targetBefore", targetBefore.toLong)
      super.serverWrite(nbt)
    }

    override def clientWrite(nbt: NBTTagCompound) = {
      nbt.setDouble("headX", headX)
      nbt.setDouble("headY", headY)
      nbt.setDouble("headZ", headZ)
      super.clientWrite(nbt)
    }

    def read(nbt: NBTTagCompound): BreakBlock = {
      //      this.digTargets = nbt.getTagList("list", NBT.TAG_LONG).iterator().asScala
      //        .collect { case num: NBTPrimitive => num.getLong }
      //        .map(BlockPos.fromLong).toList
      this.headX = nbt.getDouble("headX")
      this.headY = nbt.getDouble("headY")
      this.headZ = nbt.getDouble("headZ")
      this
    }
  }

  class CheckDrops(quarry2: TileQuarry2, y: Int) extends QuarryAction {
    var finished = false

    override def action(target: BlockPos): Unit = {
      val r = quarry2.area
      quarry2.gatherDrops(quarry2.getWorld,
        new AxisAlignedBB(r.xMin - 2, y - 3, r.zMin - 2, r.xMax + 2, y + 2, r.zMax + 2))
      finished = true
    }

    override def nextTarget() = BlockPos.ORIGIN

    override def nextAction(quarry2: TileQuarry2) = none

    override def canGoNext(quarry: TileQuarry2) = finished

    override def mode = TileQuarry2.checkDrops

    override def serverWrite(nbt: NBTTagCompound) = {
      nbt.setInteger("y", y)
      super.serverWrite(nbt)
    }
  }

  def digTargets(r: TileQuarry2.Area, pos: BlockPos, y: Int, log: Boolean = true) = {
    val firstX = near(pos.getX, r.xMin + 1, r.xMax - 1)
    val lastX = far(pos.getX, r.xMin + 1, r.xMax - 1)
    val firstZ = near(pos.getZ, r.zMin + 1, r.zMax - 1)
    val lastZ = far(pos.getZ, r.zMin + 1, r.zMax - 1)
    if (log) QuarryPlus.LOGGER.info(MARKER, s"Making targets list of breaking blocks. y=$y $r, firstX=$firstX, lastX=$lastX firstZ=$firstZ, lastZ=$lastZ")
    val list = Range.inclusive(firstZ, lastZ, signum(firstZ, lastZ))
      .map(z => Range.inclusive(firstX, lastX, signum(firstX, lastX)).map(x => new BlockPos(x, y, z)))
      .zip(Stream.iterate(true)(b => !b))
      .flatMap {
        case (p1, true) => p1
        case (p2, false) => p2.reverse
      }.toList
    list
  }

  def insideFrameArea(r: TileQuarry2.Area) = {
    (for (x <- Range.inclusive(r.xMin, r.xMax).reverse;
          z <- Range.inclusive(r.zMin, r.zMax);
          y <- Range.inclusive(r.yMin, r.yMax).reverse) yield new BlockPos(x, y, z)).toList
  }

  def near[A](pos: A, x1: A, x2: A)(implicit proxy: Numeric[A]): A = {
    val c = (proxy.minus _).curried(pos) andThen proxy.abs
    List(x1, x2).reduceLeft[A] { case (b, a) => if (proxy.lt(c(a), c(b))) a else b }
  }

  def far[A](pos: A, x1: A, x2: A)(implicit proxy: Numeric[A]): A = {
    val c = (proxy.minus _).curried(pos) andThen proxy.abs
    List(x1, x2).reduceRight[A] { case (a, b) => if (proxy.gt(c(a), c(b))) a else b }
  }

  def checkPlaceable(world: World, pos: BlockPos, state: IBlockState, toPlace: IBlockState): Boolean = {
    world.isAirBlock(pos) || state == toPlace
  }

  def checkBreakable(world: World, pos: BlockPos, state: IBlockState, modules: Seq[IModule]): Boolean = {
    !world.isAirBlock(pos) &&
      state.getBlockHardness(world, pos) >= 0 &&
      !state.getBlockHardness(world, pos).isInfinity &&
      !modules.flatMap(IModule.replaceBlocks(pos.getY)).contains(state) &&
      (!TilePump.isLiquid(state) || modules.exists(IModule.hasPumpModule))
  }

  def playSound(state: IBlockState, world: World, pos: BlockPos): Unit = {
    val sound = state.getBlock.getSoundType(state, world, pos, null)
    world.playSound(null, pos, sound.getBreakSound, SoundCategory.BLOCKS, (sound.getVolume + 1.0F) / 2.0F, sound.getPitch * 0.8F)
  }

  val getNamed: (NBTTagCompound, String) => NBTTagCompound = _.getCompoundTag(_)
  val loadFromNBT: NBTTagCompound => TileQuarry2 => QuarryAction = nbt => quarry => {
    val mode = nbt.getString(mode_nbt)
    val action = mode match {
      case TileQuarry2.none.toString => none
      case TileQuarry2.waiting.toString => waiting
      case TileQuarry2.buildFrame.toString => new MakeFrame(quarry)
      case TileQuarry2.breakBlock.toString => new BreakBlock(quarry, nbt.getInteger("y"), BlockPos.fromLong(nbt.getLong("targetBefore")))
      case TileQuarry2.breakInsideFrame.toString => new BreakInsideFrame(quarry)
      case TileQuarry2.checkDrops.toString => new CheckDrops(quarry, nbt.getInteger("y"))
      case _ => none
    }
    action match {
      case QuarryAction.none | QuarryAction.waiting => action
      case makeFrame: MakeFrame =>
        makeFrame.read(nbt)
      case breakBlock: BreakBlock =>
        breakBlock.read(nbt)
      case breakInsideFrame: BreakInsideFrame =>
        breakInsideFrame.read(nbt)
      case _ => none

    }
  }
  val load: (TileQuarry2, NBTTagCompound, String) => QuarryAction = {
    case (q, t, s) => loadFromNBT(getNamed(t, s))(q)
  }

  val signum = (a: Int, b: Int) => if (a == b) 1 else (b - a).signum

  implicit val actionToNbt: QuarryAction NBTWrapper NBTTagCompound = action => action.serverWrite(new NBTTagCompound)

}
