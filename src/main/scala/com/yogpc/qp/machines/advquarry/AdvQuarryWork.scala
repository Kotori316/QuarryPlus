package com.yogpc.qp.machines.advquarry

import cats.data._
import cats.implicits._
import com.yogpc.qp._
import com.yogpc.qp.machines.PowerManager
import com.yogpc.qp.machines.base.{Area, EnergyUsage, IModule}
import com.yogpc.qp.machines.pump.TilePump
import com.yogpc.qp.machines.quarry.QuarryFakePlayer
import com.yogpc.qp.utils.Holder
import net.minecraft.block.Blocks
import net.minecraft.entity.Entity
import net.minecraft.entity.item.ItemEntity
import net.minecraft.nbt.CompoundNBT
import net.minecraft.util.math.{AxisAlignedBB, BlockPos}
import net.minecraft.util.{Direction, EntityPredicates, Hand}
import net.minecraft.world.World
import net.minecraft.world.server.ServerWorld
import net.minecraftforge.common.util.Constants.NBT
import org.apache.logging.log4j.MarkerManager

trait AdvQuarryWork {
  val name: String

  def tick(tile: TileAdvQuarry): Unit

  def target: BlockPos

  def next(tile: TileAdvQuarry): AdvQuarryWork

  def goNext(tile: TileAdvQuarry): Boolean

  def serverWrite(nbt: CompoundNBT): CompoundNBT = {
    nbt.putString(AdvQuarryWork.NBT_key_mode, name)
    nbt
  }
}

object AdvQuarryWork {
  final val MARKER = MarkerManager.getMarker("ADV_QUARRY_ACTION")
  final val NBT_key_mode = "mode"
  final val NBT_key_target = "target"
  final val NBT_name_none = "None"
  final val NBT_name_waiting = "Waiting"
  final val NBT_name_frame = "MakeFrame"
  final val NBT_name_break = "BreakBlock"
  final val NBT_name_liquid = "RemoveLiquid"

  object none extends AdvQuarryWork {
    override val target = BlockPos.ZERO

    override def next(tile: TileAdvQuarry): AdvQuarryWork = none

    def goNext(tile: TileAdvQuarry): Boolean = false

    override def tick(tile: TileAdvQuarry): Unit = ()

    override val name = NBT_name_none
  }

  object waiting extends AdvQuarryWork {
    override val target = BlockPos.ZERO

    override def next(tile: TileAdvQuarry) = new MakeFrame(tile.area)

    def goNext(tile: TileAdvQuarry): Boolean = !Config.common.noEnergy.get() && tile.getStoredEnergy > tile.getMaxStored / 10

    override def tick(tile: TileAdvQuarry): Unit = ()

    override val name = NBT_name_waiting
  }

  class MakeFrame(area: Area) extends AdvQuarryWork {
    var framePoses = Area.getFramePoses(area)
    var mTarget = framePoses.head

    override def target = mTarget

    override def next(tile: TileAdvQuarry) = new BreakBlock(tile.area, None)

    override def serverWrite(nbt: CompoundNBT) = {
      super.serverWrite(nbt)
      nbt.putLong(NBT_key_target, target.toLong)
      nbt
    }

    override def tick(tile: TileAdvQuarry): Unit = {
      var i = 0
      while (i < 4) {
        i += 1
        if (framePoses.nonEmpty) {
          if (tryPlaceFrame(tile, target)) {
            framePoses = framePoses.tail.dropWhile(p => tile.getWorld.getBlockState(p).getBlock == Holder.blockFrame)
            mTarget = framePoses.headOption.getOrElse(BlockPos.ZERO)
          }
        }
      }
    }

    def tryPlaceFrame(tile: TileAdvQuarry, pos: BlockPos): Boolean = {
      if (pos == tile.getPos) true // Go ahead
      else if (!tile.getWorld.isAirBlock(pos)) {
        // Break block before placing frame
        val state = tile.getWorld.getBlockState(pos)
        if (state.getBlock == Holder.blockFrame) {
          true // Go ahead
        } else {
          tile.breakBlock(pos, searchReplacer = false) match {
            case Ior.Left(a) => Reason.printNonEnergy(a); !a.isEnergyIssue
            case Ior.Right(_) =>
              if (PowerManager.useEnergyFrameBuild(tile, tile.enchantments.unbreaking)) {
                tile.getWorld.setBlockState(target, Holder.blockFrame.getDefaultState)
                true
              } else {
                false
              }
            case Ior.Both(a, _) => Reason.printNonEnergy(a)
              if (tile.getWorld.isAirBlock(pos) && PowerManager.useEnergyFrameBuild(tile, tile.enchantments.unbreaking)) {
                tile.getWorld.setBlockState(target, Holder.blockFrame.getDefaultState)
                true
              } else {
                !a.isEnergyIssue
              }
          }
        }
      } else {
        if (PowerManager.useEnergyFrameBuild(tile, tile.enchantments.unbreaking)) {
          tile.getWorld.setBlockState(target, Holder.blockFrame.getDefaultState)
          true
        } else {
          false
        }
      }

    }

    override val name = NBT_name_frame

    override def goNext(tile: TileAdvQuarry) = framePoses.isEmpty
  }

  class BreakBlock(area: Area, previous: Option[BlockPos]) extends AdvQuarryWork {
    val numberInTick: Int = {
      val length = (area.xMax + area.zMax - area.xMin - area.zMin + 2) / 2
      Math.max(length / 128, 1)
    }
    var targetList = Area.posesInArea(area,
      previous.map(p => (x: Int, _: Int, z: Int) => z > p.getZ || x >= p.getX).getOrElse((_, _, _) => true))

    override def next(tile: TileAdvQuarry) = new RemoveLiquid(tile.area, None)

    override def serverWrite(nbt: CompoundNBT) = {
      super.serverWrite(nbt)
      nbt.putLong(NBT_key_target, target.toLong)
      nbt
    }

    override def tick(tile: TileAdvQuarry): Unit = {
      @scala.annotation.tailrec
      def loop(pos: BlockPos, count: Int, list: List[Reason]): List[Reason] = {
        val targets = Range(tile.yLevel, area.yMin).reverse.map(yPos => pos.copy(y = yPos))
        checkWater(tile.getWorld, targets)
        val storage1 = new AdvStorage
        if (pos.getX % 3 == 0) {
          import scala.jdk.CollectionConverters._
          // drop check
          val aabb = new AxisAlignedBB(pos.getX - 6, tile.yLevel - 3, pos.getZ - 6, pos.getX + 6, pos.getY, pos.getZ + 6)
          tile.getWorld.getEntitiesWithinAABB[ItemEntity](classOf[ItemEntity], aabb, EntityPredicates.IS_ALIVE)
            .asScala.foreach { e =>
            storage1.insertItem(e.getItem)
            QuarryPlus.proxy.removeEntity(e)
          }
          val orbs = tile.getWorld.getEntitiesWithinAABB(classOf[Entity], aabb).asScala.toList
          tile.modules.foreach(_.invoke(IModule.CollectingItem(orbs)))
        }

        if (targets.forall(tile.getWorld.isAirBlock)) {
          tile.storage.addAll(storage1, log = true)
          val searchEnergy = PowerManager.calcEnergyAdvSearch(tile.enchantments.unbreaking, pos.getY - tile.yLevel + 1)
          if (tile.useEnergy(searchEnergy, searchEnergy, true, EnergyUsage.ADV_CHECK_BLOCK) == searchEnergy) {
            targetList = targetList.tail
            if (targetList.nonEmpty) {
              loop(targetList.head, count + 1, Reason.allAir(pos, count) :: list)
            } else {
              // Work finished
              list
            }
          } else {
            Reason.energy(EnergyUsage.ADV_CHECK_BLOCK, searchEnergy, tile.getStoredEnergy, count) :: list
          }
        } else {
          // Time to remove blocks.
          val energy = targets
            .map { p =>
              val state = tile.getWorld.getBlockState(p)
              state.getBlockHardness(tile.getWorld, p) match {
                case h if TileAdvQuarry.isUnbreakable(h, state) => 0
                case _ if state.getMaterial.isLiquid => PowerManager.calcEnergyPumpDrain(tile.enchantments.unbreaking, 1L, 0L)
                case hardness => PowerManager.calcEnergyBreak(hardness, tile.enchantments)
              }
            }.sum
          if (tile.useEnergy(energy, energy, false, EnergyUsage.ADV_BREAK_BLOCK) == energy) {
            tile.useEnergy(energy, energy, true, EnergyUsage.ADV_BREAK_BLOCK)
            val fakePlayer = QuarryFakePlayer.get(tile.getWorld.asInstanceOf[ServerWorld], target)
            val pickaxe = tile.getEnchantedPickaxe()
            fakePlayer.setHeldItem(Hand.MAIN_HAND, pickaxe)
            targets.map { p =>
              val state = tile.getWorld.getBlockState(p)
              tile.gatherItemDrops(p, searchReplacer = true, state, fakePlayer, pickaxe)
            }.foldRight((List.empty[Reason], storage1)) { case (ior, (l, s)) =>
              ior.foreach(s.addAll(_, log = false)); (ior.left.toList ::: l) -> s
            } match {
              case (reasons, storage) =>
                tile.storage.addAll(storage, log = true)

                val searchEnergy = PowerManager.calcEnergyAdvSearch(tile.enchantments.unbreaking, pos.getY - tile.yLevel + 1)
                if (tile.useEnergy(searchEnergy, searchEnergy, true, EnergyUsage.ADV_CHECK_BLOCK) == searchEnergy) {
                  targetList = targetList.tail
                  reasons ::: list
                } else {
                  Reason.energy(EnergyUsage.ADV_CHECK_BLOCK, searchEnergy, tile.getStoredEnergy, count) ::
                    reasons ::: list
                }
            }
          } else {
            Reason.energy(EnergyUsage.ADV_BREAK_BLOCK, energy, tile.getStoredEnergy, count) :: list
          }
        }
      }

      var i = 0
      while (i < numberInTick) {
        i += 1
        if (targetList.nonEmpty) {
          val reasons = loop(targetList.head, 0, Nil)
          reasons.reverse.foreach(Reason.printNonEnergy)
        }
      }
    }

    def checkWater(world: World, targets: Seq[BlockPos]): Unit = {
      val pos1 = targets.head
      val flags = Array(pos1.getX == area.xMin, pos1.getX == area.xMax, pos1.getZ == area.zMin, pos1.getZ == area.zMax)
      if (flags.exists(identity)) {
        targets.foreach { pos =>
          if (flags(0)) { //-x
            checkAndSetFrame(world, pos.offset(Direction.WEST))
            if (flags(2)) { //-z, -x
              checkAndSetFrame(world, pos.offset(Direction.NORTH, Direction.WEST))
            }
            else if (flags(3)) { //+z, -x
              checkAndSetFrame(world, pos.offset(Direction.SOUTH, Direction.WEST))
            }
          }
          else if (flags(1)) { //+x
            checkAndSetFrame(world, pos.offset(Direction.EAST))
            if (flags(2)) { //-z, +x
              checkAndSetFrame(world, pos.offset(Direction.NORTH, Direction.EAST))
            }
            else if (flags(3)) { //+z, +x
              checkAndSetFrame(world, pos.offset(Direction.SOUTH, Direction.EAST))
            }
          }
          if (flags(2)) { //-z
            checkAndSetFrame(world, pos.offset(Direction.NORTH))
          }
          else if (flags(3)) { //+z
            checkAndSetFrame(world, pos.offset(Direction.SOUTH))
          }
        }
      }
    }

    def checkAndSetFrame(world: World, thatPos: BlockPos): Unit = {
      if (TilePump.isLiquid(world.getBlockState(thatPos))) {
        world.setBlockState(thatPos, Holder.blockFrame.getDammingState)
      }
    }

    override def goNext(tile: TileAdvQuarry) = targetList.isEmpty

    override val name = NBT_name_break

    override def target = targetList.headOption.getOrElse(BlockPos.ZERO)
  }

  class RemoveLiquid(area: Area, previous: Option[BlockPos]) extends AdvQuarryWork {
    val numberInTick: Int = {
      val length = (area.xMax + area.zMax - area.xMin - area.zMin + 2) / 2
      Math.max(length / 128, 1)
    }
    var targetList = Area.posesInArea(area,
      previous.map(p => (x: Int, _: Int, z: Int) => z > p.getZ || x >= p.getX).getOrElse((_, _, _) => true))

    override def target = targetList.headOption.getOrElse(BlockPos.ZERO)

    override def next(tile: TileAdvQuarry): AdvQuarryWork = none

    override def serverWrite(nbt: CompoundNBT) = {
      super.serverWrite(nbt)
      nbt.putLong(NBT_key_target, target.toLong)
      nbt
    }

    override def tick(tile: TileAdvQuarry): Unit = {
      import scala.jdk.CollectionConverters._
      val aabb = new AxisAlignedBB(area.xMin - 6, tile.yLevel - 3, area.zMin - 6, area.xMax + 6, area.yMax, area.zMax + 6)
      tile.getWorld.getEntitiesWithinAABB[ItemEntity](classOf[ItemEntity], aabb, EntityPredicates.IS_ALIVE)
        .forEach { e =>
          tile.storage.insertItem(e.getItem)
          QuarryPlus.proxy.removeEntity(e)
        }
      val orbs = tile.getWorld.getEntitiesWithinAABB(classOf[Entity], aabb).asScala.toList
      tile.modules.foreach(_.invoke(IModule.CollectingItem(orbs)))
      var i = 0
      var poses = 0
      while (i < numberInTick * 32) {
        i += 1
        targetList match {
          case Nil => i = numberInTick * 32 + 1 // Finished
          case head :: tl =>
            Range(tile.yLevel, head.getY).map(y => head.copy(y = y)).foreach { p =>
              val state = tile.getWorld.getBlockState(p)
              if (TilePump.isLiquid(state)) {
                tile.getWorld.setBlockState(p, Blocks.AIR.getDefaultState)
                poses = 1 |+| poses
              }
            }
            targetList = tl
        }
      }
      if (!poses.isEmpty)
        QuarryPlus.LOGGER.debug(AdvQuarryWork.MARKER, "Removed fluids  " + poses)
    }

    override def goNext(tile: TileAdvQuarry) = targetList.isEmpty

    override val name = NBT_name_liquid
  }

  private[this] final val loadNone: PartialFunction[CompoundNBT, AdvQuarryWork] = Function.unlift { nbt: CompoundNBT =>
    if (!nbt.contains(NBT_key_mode, NBT.TAG_STRING) || nbt.getString(NBT_key_mode) == NBT_name_none) Some(none)
    else None
  }
  private[this] final val loadWaiting: PartialFunction[CompoundNBT, AdvQuarryWork] = Function.unlift { nbt: CompoundNBT =>
    if (nbt.getString(NBT_key_mode) == NBT_name_waiting) Some(waiting)
    else None
  }

  private[this] final val loadFrame = (t: TileAdvQuarry) => Function.unlift { nbt: CompoundNBT =>
    if (nbt.getString(NBT_key_mode) == NBT_name_frame) {
      val makeFrame = new MakeFrame(t.area)
      makeFrame.mTarget = BlockPos.fromLong(nbt.getLong(NBT_key_target))
      Some(makeFrame)
    } else None
  }

  private[this] final val loadBreak = (t: TileAdvQuarry) => Function.unlift { nbt: CompoundNBT =>
    if (nbt.getString(NBT_key_mode) == NBT_name_break) {
      val pos = BlockPos.fromLong(nbt.getLong(NBT_key_target))
      Some(new BreakBlock(t.area, Option(pos)))
    } else None
  }

  private[this] final val loadLiquid = (t: TileAdvQuarry) => Function.unlift { nbt: CompoundNBT =>
    if (nbt.getString(NBT_key_mode) == NBT_name_liquid) {
      val pos = BlockPos.fromLong(nbt.getLong(NBT_key_target))
      Some(new RemoveLiquid(t.area, Option(pos)))
    } else None
  }

  private[this] final val loader = List(
    (_: TileAdvQuarry) => loadNone,
    (_: TileAdvQuarry) => loadWaiting,
    loadFrame,
    loadBreak,
    loadLiquid,
  )
  val load = (t: TileAdvQuarry) => loader.map(_.apply(t)).fold(PartialFunction.empty) { case (a, b) => a orElse b }

  implicit val AdvQuarryWork2NBT: NBTWrapper[AdvQuarryWork, CompoundNBT] = work => work.serverWrite(new CompoundNBT)
}
