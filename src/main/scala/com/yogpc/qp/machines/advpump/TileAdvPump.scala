package com.yogpc.qp.machines.advpump

import java.util

import com.yogpc.qp.compat.FluidStore
import com.yogpc.qp.machines.TranslationKeys
import com.yogpc.qp.machines.base._
import com.yogpc.qp.machines.pump.TilePump
import com.yogpc.qp.utils.Holder
import com.yogpc.qp.{Config, QuarryPlus, _}
import net.minecraft.block.IBucketPickupHandler
import net.minecraft.block.state.IBlockState
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.fluid.Fluid
import net.minecraft.init.{Blocks, Fluids}
import net.minecraft.inventory.InventoryHelper
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.{TextComponentString, TextComponentTranslation}
import net.minecraft.util.{EnumFacing, ITickable, ResourceLocation}
import net.minecraft.world.IInteractionObject
import net.minecraftforge.api.distmarker.{Dist, OnlyIn}
import net.minecraftforge.common.capabilities.Capability

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
  * @see [buildcraft.factory.tile.TilePump]
  */
class TileAdvPump extends APowerTile(Holder.advPumpType)
  with IEnchantableTile with ITickable with IDebugSender with IChunkLoadTile with IInteractionObject {

  import TileAdvPump._

  var placeFrame = true
  var delete = false
  private[this] var finished = true
  private[this] var toStart = false
  //Config.content.pumpAutoStart
  private[this] var queueBuilt = false
  private[this] var skip = false
  private[this] var ench = TileAdvPump.PEnch.defaultEnch
  private[this] var target: BlockPos = BlockPos.ORIGIN
  private[this] var toDig: List[BlockPos] = Nil
  private[this] var toDelete: List[BlockPos] = Nil
  private[this] var inRange: Set[BlockPos] = Set.empty
  private[this] val paths = mutable.Map.empty[BlockPos, List[BlockPos]]
  private[this] val FACINGS = List(EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST)

  override def isWorking: Boolean = !finished

  override def G_ReInit(): Unit = {
    configure(ench.getReceiveEnergy, 1024 * APowerTile.MicroJtoMJ)
    finished = true
    queueBuilt = false
    skip = false
    target = BlockPos.ORIGIN
    toDig = Nil
    paths.clear()
    if (!ench.square && ench.fortune >= 3) {
      EnumFacing.Plane.HORIZONTAL.iterator().asScala.map(f => getWorld.getTileEntity(getPos.offset(f))).collectFirst {
        case marker: IMarker if marker.hasLink => marker
      }.foreach(marker => {
        ench = ench.copy(start = marker.min(), end = marker.max())
        marker.removeFromWorldWithItem().asScala.foreach(s =>
          InventoryHelper.spawnItemStack(getWorld, getPos.getX + 0.5, getPos.getY + 1, getPos.getZ + 0.5, s))
      })
    }
  }

  override def tick(): Unit = {
    super.tick()
    if (!getWorld.isRemote && !machineDisabled) {
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
          if (target == BlockPos.ORIGIN) {
            buildWay()
            nextPos()
          }
          pump()
        }
      }
    }
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
        case Nil => target = BlockPos.ORIGIN
      }
    }
  }

  def buildWay(): Unit = {
    val checked = mutable.Set.empty[BlockPos]
    val nextPosesToCheck = new ArrayBuffer[BlockPos](getPos.getY)
    var fluid: Fluid = Fluids.WATER
    toDig = Nil
    paths.clear()

    getWorld.profiler.startSection("Depth")

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
      getWorld.profiler.endSection()
      return
    }

    getWorld.profiler.endStartSection("Wide")
    while (nextPosesToCheck.nonEmpty) {
      val copied = nextPosesToCheck.toArray
      nextPosesToCheck.clear()
      for (posToCheck <- copied; offset <- FACINGS) {
        val offsetPos = posToCheck.offset(offset)
        if (ench.inRange(getPos, offsetPos)) {
          if (checked.add(offsetPos)) {
            val state = getWorld.getBlockState(offsetPos)
            if (findFluid(state) == fluid) {
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
    getWorld.profiler.endSection()
  }

  def pump(): Unit = {
    var break = false
    var fluid = Fluids.EMPTY
    var amount = 0
    var counter = 0
    while (!break && counter < ench.maxAmount / 1000) {
      paths.get(target) match {
        case Some(posList) =>
          val energy = ench.getEnergy(placeFrame)
          val state = getWorld.getBlockState(target)
          val isLiquid = TilePump.isLiquid(state)
          val isSource = TilePump.isLiquid(state, true, getWorld, target)
          if (isLiquid && !isSource) {
            getWorld.removeBlock(target)
            nextPos()
          } else if (useEnergy(energy, energy, true, EnergyUsage.ADV_PUMP_FLUID) == energy) {
            //TODO rewrite
            if (isSource && posList.forall(pos => TilePump.isLiquid(getWorld.getBlockState(pos)))) {
              fluid = state.getFluidState.getFluid
              amount += 1000
              replaceFluidBlock(target)
              nextPos()
              counter += 1
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
    push(fluid, amount)
  }

  def push(fluid: Fluid, amount: Int): Unit = {
    if (!delete)
      FluidStore.injectToNearTile(getWorld, getPos, fluid, amount)
  }

  def start(): Unit = {
    if (!isWorking)
      toStart = true
  }

  def replaceFluidBlock(pos: BlockPos): Unit = {
    val state = getWorld.getBlockState(pos)
    state.getBlock match {
      case h: IBucketPickupHandler => h.pickupFluid(getWorld, pos, state)
      case _ => getWorld.setBlockState(pos, Blocks.AIR.getDefaultState)
    }
    if (placeFrame)
      EnumFacing.Plane.HORIZONTAL.iterator().asScala.foreach(facing => {
        val offset = pos.offset(facing)
        // TODO CHECK
        if (!inRange.contains(offset) && TilePump.isLiquid(getWorld.getBlockState(offset))) {
          getWorld.setBlockState(offset, Holder.blockFrame.getDammingState)
        }
      })
  }

  private def changeState(working: Boolean, state: IBlockState): Unit = {
    getWorld.setBlockState(getPos, state.`with`(QPBlock.WORKING, Boolean.box(working)))
  }

  private def findFluid(state: IBlockState) = {
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

  override def getDebugMessages: java.util.List[TextComponentString] = {
    List("Range = " + ench.distance,
      "target : " + target,
      "Finished : " + finished,
      "Ench : " + ench,
      //      "FluidType : " + FluidHandler.getFluidType,
      //      "FluidAmount : " + FluidHandler.getAmount,
      //      "Pumped : " + FluidHandler.amountPumped,
      "Delete : " + delete,
      "To Start : " + toStart,
      "Start pos : " + ench.start,
      "End pos : " + ench.end).map(toComponentString).asJava
  }

  override def read(nbt: NBTTagCompound): Unit = {
    super.read(nbt)
    target = BlockPos.fromLong(nbt.getLong(NBT_POS))
    ench = PEnch.readFromNBT(nbt.getCompound(NBT_P_ENCH))
    finished = nbt.getBoolean(NBT_FINISHED)
    placeFrame = nbt.getBoolean(NBT_PLACE_FRAME)
    delete = nbt.getBoolean(NBT_DELETE)
    //    FluidHandler.readFromNBT(nbt.getCompound(NBT_FluidHandler))
  }

  override def write(nbt: NBTTagCompound): NBTTagCompound = {
    nbt.putLong(NBT_POS, target.toLong)
    nbt.putBoolean(NBT_FINISHED, finished)
    nbt.putBoolean(NBT_PLACE_FRAME, placeFrame)
    nbt.putBoolean(NBT_DELETE, delete)
    nbt.put(NBT_P_ENCH, ench.toNBT)
    //    nbt.put(NBT_FluidHandler, FluidHandler.toNBT)
    super.write(nbt)
  }

  override def getCapability[T](cap: Capability[T], side: EnumFacing) = super.getCapability(cap, side)

  override def remove(): Unit = {
    releaseTicket()
    super.remove()
  }

  @OnlyIn(Dist.CLIENT)
  def receiveStatusMessage(placeFrame: Boolean, nbt: NBTTagCompound): Runnable = () => {
    TileAdvPump.this.placeFrame = placeFrame
    TileAdvPump.this.read(nbt)
  }

  def toggleDelete(): Unit = delete = !delete

  /*private object FluidHandler extends IFluidHandler with INBTWritable with INBTReadable[IFluidHandler] {

    private[this] final val fluidStacks = new ListBuffer[FluidStack]
    var amountPumped = 0l

    override def fill(resource: FluidStack, doFill: Boolean): Int = 0

    def fillInternal(resource: FluidStack, doFill: Boolean): Int = {
      if (resource == null || resource.amount <= 0)
        return 0
      fluidStacks.find(_ == resource) match {
        case Some(stack) =>
          val nAmount = stack.amount + resource.amount
          if (nAmount <= ench.maxAmount) {
            if (doFill) {
              stack.amount = nAmount
              amountPumped += resource.amount
            }
            resource.amount
          } else {
            if (doFill) {
              stack.amount = ench.maxAmount
              amountPumped += nAmount - ench.maxAmount
            }
            nAmount - stack.amount
          }
        case None =>
          if (doFill) {
            fluidStacks += resource.copy()
            amountPumped += resource.amount
          }
          resource.amount
      }
    }

    override def drain(resource: FluidStack, doDrain: Boolean): FluidStack = {
      fluidStacks.find(_ == resource) match {
        case None => null
        case Some(stack) => drainInternal(resource, stack, doDrain)
      }
    }

    override def drain(maxDrain: Int, doDrain: Boolean): FluidStack = {
      fluidStacks.headOption match {
        case None => null
        case Some(stack) => drainInternal(stack.copyWithAmount(maxDrain), stack, doDrain)
      }
    }

    private def drainInternal(kind: FluidStack, source: FluidStack, doDrain: Boolean): FluidStack = {
      if (kind == null || kind.amount <= 0) {
        return null
      }
      if (kind.amount >= source.amount) {
        val extract = source.amount
        if (doDrain) fluidStacks.remove(fluidStacks.indexOf(kind))
        kind.copyWithAmount(extract)
      } else {
        val nAmount = source.amount - kind.amount
        if (doDrain) source.setAmount(nAmount)
        kind
      }
    }

    override def getTankProperties: Array[IFluidTankProperties] = {
      if (fluidStacks.isEmpty) {
        IDummyFluidHandler.emptyPropertyArray
      } else {
        fluidStacks.map(s => new FluidTankProperties(s, ench.maxAmount, false, true)).toArray
      }
    }

    def getFluidType: String = {
      val str = fluidStacks.flatMap(s => Option(s.getFluid).toList).map(_.getName).mkString(", ")
      if (str.isEmpty) {
        "None"
      } else {
        str
      }
    }

    def getAmount: Int =
      if (fluidStacks.nonEmpty) fluidStacks.head.amount
      else 0

    def canPump: Boolean =
      if (fluidStacks.isEmpty) true
      else fluidStacks.forall(_.amount <= ench.maxAmount / 2)

    override def writeToNBT(nbt: NBTTagCompound): NBTTagCompound = {
      nbt.setTag(NBT_pumped, amountPumped.toNBT)
      nbt.setTag(NBT_liquids, fluidStacks.map(_.toNBT).foldLeft(NBTBuilder.empty) { case (l, t) => l.appendTag(t) }.toList)
      nbt
    }

    override def readFromNBT(nbt: NBTTagCompound): IFluidHandler = {
      amountPumped = nbt.getLong(NBT_pumped)
      val list = nbt.getTagList(NBT_liquids, NBT.TAG_COMPOUND)
      for (t <- list.tagIterator;
           f <- FluidStack.loadFluidStackFromNBT(t).toOption)
        fluidStacks += f
      this
    }
  }*/
  override def createContainer(playerInventory: InventoryPlayer, playerIn: EntityPlayer) = new ContainerAdvPump(this, playerIn)

  override def getGuiID = GUI_ID

  override def getName = new TextComponentTranslation(getDebugName)

  override def hasCustomName = false

  override def getCustomName = getName

  override def getDisplayName = getName
}

object TileAdvPump {

  final val GUI_ID = QuarryPlus.modID + ":gui_" + QuarryPlus.Names.advpump
  final val SYMBOL = Symbol("AdvancedPump")
  private final val NBT_P_ENCH = "nbt_pump_ench"
  private final val NBT_FluidHandler = "FluidHandler"
  private final val NBT_pumped = "amountPumped"
  private final val NBT_liquids = "liquids"

  final val NBT_POS = "targetPos"
  final val NBT_FINISHED = "finished"
  final val NBT_PLACE_FRAME = "placeFrame"
  final val NBT_DELETE = "delete"
  private[this] final val defaultBaseEnergy = Seq(10, 8, 6, 4).map(_ * APowerTile.MicroJtoMJ)
  private[this] final val defaultReceiveEnergy = Seq(32, 64, 128, 256, 512, 1024).map(_ * APowerTile.MicroJtoMJ)
  implicit val pEnchNbt: NBTWrapper[PEnch, NBTTagCompound] = _.writeToNBT(new NBTTagCompound)

  case class PEnch(efficiency: Int, unbreaking: Int, fortune: Int, silktouch: Boolean, start: BlockPos, end: BlockPos) {
    require(efficiency >= 0)
    require(unbreaking >= 0)
    require(fortune >= 0)
    val square: Boolean = start != BlockPos.ORIGIN && end != BlockPos.ORIGIN

    def writeToNBT(nbt: NBTTagCompound): NBTTagCompound = {
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

    val maxAmount: Int = 128 * 1000 * (efficiency + 1)

    val baseEnergy = defaultBaseEnergy(if (unbreaking >= 3) 3 else unbreaking)

    def getEnergy(placeFrame: Boolean) = {
      baseEnergy * (if (placeFrame) 2.5 else 1).toLong
    }

    val getReceiveEnergy = if (efficiency >= 5) defaultReceiveEnergy(5) else defaultReceiveEnergy(efficiency)

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
  }

  object PEnch {
    val defaultEnch = PEnch(efficiency = 0, unbreaking = 0, fortune = 0, silktouch = false, BlockPos.ORIGIN, BlockPos.ORIGIN)

    def readFromNBT(tag: NBTTagCompound): PEnch = {
      if (!tag.isEmpty) {
        PEnch(tag.getInt("efficiency"), tag.getInt("unbreaking"), tag.getInt("fortune"), tag.getBoolean("silktouch"),
          BlockPos.fromLong(tag.getLong("start")), BlockPos.fromLong(tag.getLong("end")))
      } else
        PEnch(efficiency = 0, unbreaking = 0, fortune = 0, silktouch = false, BlockPos.ORIGIN, BlockPos.ORIGIN)
    }
  }

}
