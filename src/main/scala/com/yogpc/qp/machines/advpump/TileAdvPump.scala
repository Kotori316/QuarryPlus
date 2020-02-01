package com.yogpc.qp.machines.advpump

import java.util

import cats._
import cats.implicits._
import com.yogpc.qp.machines.TranslationKeys
import com.yogpc.qp.machines.base._
import com.yogpc.qp.machines.modules.IModuleItem
import com.yogpc.qp.machines.pump.TilePump
import com.yogpc.qp.machines.quarry.ContainerQuarryModule
import com.yogpc.qp.utils.Holder
import com.yogpc.qp.{Config, QuarryPlus, _}
import net.minecraft.block.{BlockState, Blocks, IBucketPickupHandler}
import net.minecraft.entity.player.{PlayerEntity, PlayerInventory, ServerPlayerEntity}
import net.minecraft.fluid.{Fluid, Fluids}
import net.minecraft.inventory.InventoryHelper
import net.minecraft.inventory.container.INamedContainerProvider
import net.minecraft.nbt.CompoundNBT
import net.minecraft.tileentity.ITickableTileEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.{StringTextComponent, TranslationTextComponent}
import net.minecraft.util.{Direction, IntReferenceHolder, ResourceLocation}
import net.minecraftforge.api.distmarker.{Dist, OnlyIn}
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.fluids.capability.{CapabilityFluidHandler, IFluidHandler}
import net.minecraftforge.fluids.{FluidAttributes, FluidStack}
import net.minecraftforge.fml.network.NetworkHooks

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.jdk.CollectionConverters._

/**
 * @see [buildcraft.factory.tile.TilePump]
 */
class TileAdvPump extends APowerTile(Holder.advPumpType)
  with IEnchantableTile
  with ITickableTileEntity
  with IDebugSender
  with IChunkLoadTile
  with INamedContainerProvider
  with HasStorage
  with ContainerQuarryModule.HasModuleInventory
  with StatusContainer.StatusProvider
  with EnchantmentHolder.EnchantmentProvider {

  import TileAdvPump._

  var placeFrame = true
  var delete = false
  private[this] var finished = true
  private[this] var toStart = false //Config.content.pumpAutoStart
  private[this] var queueBuilt = false
  private[this] var skip = false
  private[this] var ench = TileAdvPump.PEnch.defaultEnch
  private[this] var target: BlockPos = BlockPos.ZERO
  private[this] var toDig: List[BlockPos] = Nil
  private[this] var toDelete: List[BlockPos] = Nil
  private[this] var inRange: Set[BlockPos] = Set.empty
  private[this] val paths = mutable.Map.empty[BlockPos, List[BlockPos]]
  private[this] val FACINGS = List(Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST)
  private[this] val storage = new TankAdvPump(Eval.always(ench.maxAmount))
  val moduleInv = new QuarryModuleInventory(5, this, _ => refreshModules(), moduleFilter)
  private[this] var modules: List[IModule] = Nil

  override def isWorking: Boolean = !finished

  override def G_ReInit(): Unit = {
    configure(ench.getReceiveEnergy, ench.getReceiveEnergy * 3)
    finished = true
    queueBuilt = false
    skip = false
    target = BlockPos.ZERO
    toDig = Nil
    paths.clear()
    if (!ench.square && ench.fortune >= 3) {
      val mayMarker = Area.getMarkersOnDirection(List.from(Direction.Plane.HORIZONTAL.iterator().asScala), getWorld, getPos)
      mayMarker.toList
        .headOption
        .foreach { marker =>
          ench = ench.copy(start = marker.min(), end = marker.max())
          marker.removeFromWorldWithItem().asScala.foreach(s =>
            InventoryHelper.spawnItemStack(getWorld, getPos.getX + 0.5, getPos.getY + 1, getPos.getZ + 0.5, s))
        }
    }
  }

  override def workInTick(): Unit = {
    modules.foreach(_.invoke(IModule.Tick(this)))
    if (finished) {
      if (toStart) {
        toStart = false
        buildWay()
        if (toDig.nonEmpty) {
          finished = false
          startWork()
          val state = getWorld.getBlockState(getPos)
          if (!state.get(QPBlock.WORKING)) {
            changeState(working = true, state)
          }
        }
      }
    } else {
      if (!queueBuilt) {
        buildWay()
        queueBuilt = true
      }
      if (skip) {
        skip = false
        nextPos()
      } else {
        if (target == BlockPos.ZERO) {
          buildWay()
          nextPos()
        }
        pump()
      }
    }
    push()
  }

  def nextPos(i: Int = 0): Unit = {
    def overflowedMessage(): Unit = {
      if (Config.common.debug) {
        QuarryPlus.LOGGER.warn("Pump overflowed")
        List("Pos = " + getPos,
          "Range = " + ench.distance,
          "target : " + target,
          "Ench : " + ench,
          //          "FluidType : " + FluidHandler.getFluidType,
          //          "FluidAmount : " + FluidHandler.getAmount,
          //          "Pumped : " + FluidHandler.amountPumped,
          "Start pos : " + ench.start,
          "End pos : " + ench.end).map("   " + _).foreach(QuarryPlus.LOGGER.warn)
      }
    }

    toDig match {
      case next :: rest => toDig = rest
        if (TilePump.isLiquid(getWorld.getBlockState(next), true, getWorld, next)) {
          target = next
        } else {
          if (i > ench.maxAmount / 1000) {
            skip = true
            overflowedMessage()
          } else
            nextPos(i + 1)
        }
      case Nil => toDelete match {
        case next :: rest => toDelete = rest
          if (TilePump.isLiquid(getWorld.getBlockState(next))) {
            target = next
          } else {
            if (i > ench.maxAmount / 1000) {
              skip = true
              overflowedMessage()
            } else
              nextPos(i + 1)
          }
        case Nil => target = BlockPos.ZERO
      }
    }
  }

  def buildWay(): Unit = {
    val checked = mutable.Set.empty[BlockPos]
    val nextPosesToCheck = new ArrayBuffer[BlockPos](getPos.getY)
    var fluid: Fluid = Fluids.WATER
    toDig = Nil
    paths.clear()

    getWorld.getProfiler.startSection("Depth")

    var downPos = ench.firstPos(getPos).down
    var flag = true
    while (flag) {
      if (downPos.getY < 0 || !ench.inRange(getPos, downPos)) {
        flag = false
      } else {
        if (!getWorld.isAirBlock(downPos)) {
          val state = getWorld.getBlockState(downPos)
          if (TilePump.isLiquid(state)) {
            checked.add(downPos)
            paths.put(downPos, List(downPos))
            if (TilePump.isLiquid(state, true, getWorld, downPos))
              toDig = downPos :: toDig
            nextPosesToCheck += downPos

            fluid = findFluid(state)
          }
          flag = false
        } else {
          downPos = downPos.down
        }
      }
    }
    if (nextPosesToCheck.isEmpty) {
      G_ReInit()
      finishWork()
      val state = getWorld.getBlockState(getPos)
      if (state.get(QPBlock.WORKING)) {
        changeState(working = false, state)
      }
      getWorld.getProfiler.endSection()
      return
    }

    getWorld.getProfiler.endStartSection("Wide")
    while (nextPosesToCheck.nonEmpty) {
      val copied = nextPosesToCheck.toArray
      nextPosesToCheck.clear()
      for (posToCheck <- copied; offset <- FACINGS) {
        val offsetPos = posToCheck.offset(offset)
        if (ench.inRange(getPos, offsetPos)) {
          if (checked.add(offsetPos)) {
            val state = getWorld.getBlockState(offsetPos)
            if (findFluid(state) isEquivalentTo fluid) {
              paths += ((offsetPos, offsetPos :: paths(posToCheck)))
              nextPosesToCheck += offsetPos
              if (TilePump.isLiquid(state, true, getWorld, offsetPos))
                toDig = offsetPos :: toDig
              else if (TilePump.isLiquid(state))
                toDelete = offsetPos :: toDelete
            }
          } //else means the pos has already checked.
        }
      }
    }
    inRange = checked.toSet
    getWorld.getProfiler.endSection()
  }

  def pump(): Unit = {
    var break = false
    while (!break && storage.canPump) {
      paths.get(target) match {
        case Some(posList) =>
          val energy = ench.getEnergy(placeFrame)
          val state = getWorld.getBlockState(target)
          val isLiquid = TilePump.isLiquid(state)
          val isSource = TilePump.isLiquid(state, true, getWorld, target)
          if (isLiquid && !isSource) {
            getWorld.removeBlock(target, false)
            nextPos()
          } else if (useEnergy(energy, energy, true, EnergyUsage.ADV_PUMP_FLUID) == energy) {
            if (isSource && posList.forall(pos => TilePump.isLiquid(getWorld.getBlockState(pos)))) {
              val stack = new FluidStack(state.getFluidState.getFluid, FluidAttributes.BUCKET_VOLUME)
              storage.insertFluid(stack)
              replaceFluidBlock(target)
              nextPos()
            } else {
              buildWay()
              nextPos()
            }
          } else {
            //Pump can't work because of lack of energy. Wait to get more energy.
            break = true
          }
        case None =>
          buildWay()
          nextPos()
          break = true
      }
    }
  }

  def push(): Unit = {
    if (delete) {
      storage.drain(Int.MaxValue, IFluidHandler.FluidAction.EXECUTE)
      return
    }
    val stack = storage.drain(Int.MaxValue, IFluidHandler.FluidAction.SIMULATE)
    if (!stack.isEmpty) {
      for (facing <- facings.value;
           tile <- Option(getWorld.getTileEntity(getPos.offset(facing)));
           handler <- tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite).asScala.value.value.toList) {
        val used = handler.fill(stack, IFluidHandler.FluidAction.EXECUTE)
        if (used > 0) {
          storage.drain(new FluidStack(stack, used), IFluidHandler.FluidAction.EXECUTE)
        }
      }
    }
  }

  def start(): Unit = {
    if (!isWorking)
      toStart = true
  }

  def replaceFluidBlock(pos: BlockPos): Unit = {
    val state = getWorld.getBlockState(pos)
    state.getBlock match {
      case h: IBucketPickupHandler => h.pickupFluid(getWorld, pos, state)
      case _ =>
        modules.foldMap(_.invoke(IModule.AfterBreak(getWorld, pos, state, getWorld.getGameTime))) match {
          case IModule.NoAction => getWorld.setBlockState(pos, Blocks.AIR.getDefaultState)
          case _ =>
        }
    }
    if (placeFrame)
      Direction.Plane.HORIZONTAL.iterator().asScala.foreach { facing =>
        val offset = pos.offset(facing)
        // TODO CHECK
        if (!inRange.contains(offset) && TilePump.isLiquid(getWorld.getBlockState(offset))) {
          getWorld.setBlockState(offset, Holder.blockFrame.getDammingState)
        }
      }
  }

  private def changeState(working: Boolean, state: BlockState): Unit = {
    getWorld.setBlockState(getPos, state.`with`(QPBlock.WORKING, Boolean.box(working)))
  }

  private def findFluid(state: BlockState) = {
    state.getFluidState.getFluid
  }

  /**
   * @return Map (Enchantment id, level)
   */
  override def getEnchantments: util.Map[ResourceLocation, Integer] = ench.getMap.collect(enchantCollector).asJava

  /**
   * @param id    Enchantment id
   * @param value level
   */
  override def setEnchantment(id: ResourceLocation, value: Short): Unit = ench = ench.set(id, value)

  override def getDebugName: String = TranslationKeys.advpump

  override def getDebugMessages: java.util.List[StringTextComponent] = {
    List("Range = " + ench.distance,
      "target : " + target,
      "Finished : " + finished,
      "Ench : " + ench,
      "FluidType : " + storage.getFluidInTank(0).show,
      "Pumped : " + storage.amountPumped / 1000 + "B",
      "Delete : " + delete,
      "To Start : " + toStart,
      "Start pos : " + ench.start,
      "End pos : " + ench.end).map(toComponentString).asJava
  }

  override def read(nbt: CompoundNBT): Unit = {
    super.read(nbt)
    target = BlockPos.fromLong(nbt.getLong(NBT_POS))
    ench = PEnch.readFromNBT(nbt.getCompound(NBT_P_ENCH))
    finished = nbt.getBoolean(NBT_FINISHED)
    placeFrame = nbt.getBoolean(NBT_PLACE_FRAME)
    delete = nbt.getBoolean(NBT_DELETE)
    storage.deserializeNBT(nbt.getCompound(NBT_FluidHandler))
    moduleInv.deserializeNBT(nbt.getCompound(NBT_ModuleInv))
  }

  override def write(nbt: CompoundNBT): CompoundNBT = {
    nbt.putLong(NBT_POS, target.toLong)
    nbt.putBoolean(NBT_FINISHED, finished)
    nbt.putBoolean(NBT_PLACE_FRAME, placeFrame)
    nbt.putBoolean(NBT_DELETE, delete)
    nbt.put(NBT_P_ENCH, ench.toNBT)
    nbt.put(NBT_FluidHandler, storage.toNBT)
    nbt.put(NBT_ModuleInv, moduleInv.toNBT)
    super.write(nbt)
  }

  override def getCapability[T](cap: Capability[T], side: Direction) =
    Cap.asJava(storage.getCapability(cap) orElse super.getCapability(cap, side).asScala)

  override def remove(): Unit = {
    releaseTicket()
    super.remove()
  }

  @OnlyIn(Dist.CLIENT)
  def receiveStatusMessage(placeFrame: Boolean, nbt: CompoundNBT): Runnable = () => {
    TileAdvPump.this.placeFrame = placeFrame
    TileAdvPump.this.read(nbt)
  }

  def toggleDelete(): Unit = delete = !delete

  override def getDisplayName = new TranslationTextComponent(getDebugName)

  override def createMenu(id: Int, i: PlayerInventory, p: PlayerEntity) = new ContainerAdvPump(id, p, getPos)

  def refreshModules(): Unit = {
    val internalModules = moduleInv.moduleItems().asScala.toList >>= (e => e.getKey.apply(e.getValue, this).toList)
    this.modules = internalModules
  }

  def openModuleInv(player: ServerPlayerEntity): Unit = {
    if (hasWorld && !world.isRemote) {
      NetworkHooks.openGui(player, new ContainerQuarryModule.InteractionObject(getPos, TranslationKeys.advpump), getPos)
    }
  }

  override def getEnchantmentHolder = ench.toHolder

  override def getStatusStrings(trackIntSeq: Seq[IntReferenceHolder]): Seq[String] = {
    val enchantmentStrings = EnchantmentHolder.getEnchantmentStringSeq(this.getEnchantmentHolder)
    val modules = if (this.modules.nonEmpty) "Modules" :: this.modules.map("  " + _.toString) else Nil
    enchantmentStrings ++ modules
  }

  override def getStorage = storage
}

object TileAdvPump {

  final val GUI_ID = QuarryPlus.modID + ":gui_" + QuarryPlus.Names.advpump
  final val SYMBOL = Symbol("AdvancedPump")
  private final val NBT_P_ENCH = "nbt_pump_ench"
  private final val NBT_FluidHandler = "FluidHandler"
  private final val NBT_ModuleInv = "moduleInv"

  final val NBT_POS = "targetPos"
  final val NBT_FINISHED = "finished"
  final val NBT_PLACE_FRAME = "placeFrame"
  final val NBT_DELETE = "delete"
  private[this] final val defaultBaseEnergy = Seq(10, 8, 5, 2).map(_ * APowerTile.MJToMicroMJ)
  private[this] final val defaultReceiveEnergy = Seq(32, 64, 128, 256, 512, 1024).map(_ * APowerTile.MJToMicroMJ)
  implicit val pEnchNbt: NBTWrapper[PEnch, CompoundNBT] = _.writeToNBT(new CompoundNBT)

  case class PEnch(efficiency: Int, unbreaking: Int, fortune: Int, silktouch: Boolean, start: BlockPos, end: BlockPos) {
    require(efficiency >= 0)
    require(unbreaking >= 0)
    require(fortune >= 0)
    val square: Boolean = start != BlockPos.ZERO && end != BlockPos.ZERO

    def writeToNBT(nbt: CompoundNBT): CompoundNBT = {
      nbt.putInt("efficiency", efficiency)
      nbt.putInt("unbreaking", unbreaking)
      nbt.putInt("fortune", fortune)
      nbt.putBoolean("silktouch", silktouch)
      nbt.putLong("start", start.toLong)
      nbt.putLong("end", end.toLong)
      nbt
    }

    import IEnchantableTile._

    def getMap = Map(EfficiencyID -> efficiency, UnbreakingID -> unbreaking,
      FortuneID -> fortune, SilktouchID -> silktouch.compare(false))

    def set(id: ResourceLocation, level: Int): PEnch = {
      id match {
        case EfficiencyID => this.copy(efficiency = level)
        case UnbreakingID => this.copy(unbreaking = level)
        case FortuneID => this.copy(fortune = level)
        case SilktouchID => this.copy(silktouch = level > 0)
        case _ => this
      }
    }

    val distance: Int = fortune match {
      case 0 => 32
      case 1 => 64
      case 2 => 96
      case _ => 128
    }

    val distanceSq: Int = distance * distance

    val maxAmount: Int = 512 * 1000 * (efficiency + 1)

    val baseEnergy = defaultBaseEnergy(if (unbreaking >= 3) 3 else unbreaking)

    def getEnergy(placeFrame: Boolean) = {
      baseEnergy * (if (placeFrame) 2.5 else 1).toLong
    }

    val getReceiveEnergy = (if (efficiency >= 5) defaultReceiveEnergy(5) else defaultReceiveEnergy(efficiency)) * (fortune + 1) * (fortune + 1)

    def inRange(tilePos: BlockPos, pos: BlockPos): Boolean = {
      if (square) {
        if (silktouch) {
          start.getX < pos.getX && pos.getX < end.getX &&
            start.getZ < pos.getZ && pos.getZ < end.getZ
        } else {
          start.getX < pos.getX && pos.getX < end.getX &&
            start.getZ < pos.getZ && pos.getZ < end.getZ &&
            tilePos.distanceSq(pos) < distanceSq
        }
      } else if (silktouch) {
        val dx = tilePos.getX - pos.getX
        val dz = tilePos.getZ - pos.getZ
        (dx * dx + dz * dz) <= distanceSq
      } else {
        tilePos.distanceSq(pos) <= distanceSq
      }
    }

    def firstPos(default: BlockPos): BlockPos = {
      if (square)
        new BlockPos((start.getX + end.getX) / 2, start.getY, (start.getZ + end.getZ) / 2)
      else
        default
    }

    override def toString: String = s"PEnch($efficiency, $unbreaking, $fortune, $silktouch)"

    def toHolder = EnchantmentHolder(efficiency, unbreaking, fortune, silktouch)
  }

  object PEnch {
    val defaultEnch = PEnch(efficiency = 0, unbreaking = 0, fortune = 0, silktouch = false, BlockPos.ZERO, BlockPos.ZERO)

    def readFromNBT(tag: CompoundNBT): PEnch = {
      if (!tag.isEmpty) {
        PEnch(tag.getInt("efficiency"), tag.getInt("unbreaking"), tag.getInt("fortune"), tag.getBoolean("silktouch"),
          BlockPos.fromLong(tag.getLong("start")), BlockPos.fromLong(tag.getLong("end")))
      } else
        PEnch(efficiency = 0, unbreaking = 0, fortune = 0, silktouch = false, BlockPos.ZERO, BlockPos.ZERO)
    }
  }

  private[this] final lazy val acceptableModule = Set(
    Holder.itemFuelModuleCreative.getSymbol,
    Holder.itemFuelModuleNormal.getSymbol,
  )

  val moduleFilter: java.util.function.Predicate[IModuleItem] = item => acceptableModule.contains(item.getSymbol)
}
