package com.yogpc.qp.tile

import java.util

import com.yogpc.qp.block.ADismCBlock
import com.yogpc.qp.gui.TranslationKeys
import com.yogpc.qp.tile.IEnchantableTile.{EfficiencyID, FortuneID, SilktouchID, UnbreakingID}
import com.yogpc.qp.tile.TileAdvPump._
import com.yogpc.qp.utils.{INBTReadable, INBTWritable}
import com.yogpc.qp.version.VersionUtil
import com.yogpc.qp.{Config, QuarryPlus, QuarryPlusI, _}
import net.minecraft.block.state.IBlockState
import net.minecraft.init.Blocks
import net.minecraft.inventory.InventoryHelper
import net.minecraft.nbt.{NBTTagCompound, NBTTagList}
import net.minecraft.util.math.{BlockPos, ChunkPos}
import net.minecraft.util.text.TextComponentString
import net.minecraft.util.{EnumFacing, ITickable}
import net.minecraftforge.common.ForgeChunkManager
import net.minecraftforge.common.ForgeChunkManager.Type
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.Constants.NBT
import net.minecraftforge.fluids.capability.{CapabilityFluidHandler, FluidTankProperties, IFluidHandler, IFluidTankProperties}
import net.minecraftforge.fluids.{Fluid, FluidRegistry, FluidStack, FluidUtil, IFluidBlock}
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer}

/**
  * @see [[buildcraft.factory.tile.TilePump]]
  */
class TileAdvPump extends APowerTile with IEnchantableTile with ITickable with IDebugSender with IChunkLoadTile {

  var placeFrame = true
  var delete = false
  private[this] var finished = true
  private[this] var toStart = Config.content.pumpAutoStart
  private[this] var queueBuilt = false
  private[this] var skip = false
  private[this] var ench = TileAdvPump.defaultEnch
  private[this] var target: BlockPos = BlockPos.ORIGIN
  private[this] var toDig: List[BlockPos] = Nil
  private[this] var toDelete: List[BlockPos] = Nil
  private[this] var inRange: Set[BlockPos] = Set.empty
  private[this] val paths = mutable.Map.empty[BlockPos, List[BlockPos]]
  private[this] val FACINGS = List(EnumFacing.UP, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.EAST)

  override def isWorking: Boolean = !finished

  override def G_ReInit(): Unit = {
    configure(ench.getReceiveEnergy, 1024d)
    finished = true
    queueBuilt = false
    skip = false
    target = BlockPos.ORIGIN
    toDig = Nil
    paths.clear()
    if (!ench.square && ench.fortune >= 3) {
      EnumFacing.HORIZONTALS.map(f => getWorld.getTileEntity(getPos.offset(f))).collectFirst {
        case marker: TileMarker if marker.link != null => marker
      }.foreach(marker => {
        ench = ench.copy(start = marker.link.minPos(), end = marker.link.maxPos())
        marker.removeFromWorldWithItem().asScala.foreach(s =>
          InventoryHelper.spawnItemStack(getWorld, getPos.getX + 0.5, getPos.getY + 1, getPos.getZ + 0.5, s))
      })
    }
  }

  override def update(): Unit = {
    super.update()
    if (!getWorld.isRemote && !machineDisabled) {
      if (finished) {
        if (toStart) {
          toStart = false
          buildWay()
          if (toDig.nonEmpty) {
            finished = false
            startWork()
            val state = getWorld.getBlockState(getPos)
            if (!state.getValue(ADismCBlock.ACTING)) {
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
      push()
    }
  }

  def nextPos(i: Int = 0): Unit = {
    def overflowedMessage(): Unit = {
      if (Config.content.debug) {
        QuarryPlus.LOGGER.warn("Pump overflowed")
        List("Pos = " + getPos,
          "Range = " + ench.distance,
          "target : " + target,
          "Ench : " + ench,
          "FluidType : " + FluidHandler.getFluidType,
          "FluidAmount : " + FluidHandler.getAmount,
          "Pumped : " + FluidHandler.amountPumped,
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
    var fluid = FluidRegistry.WATER
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
      if (state.getValue(ADismCBlock.ACTING)) {
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
    while (FluidHandler.canPump && !break) {
      val option = paths.get(target)
      if (option.nonEmpty) {
        val energy = ench.getEnergy(placeFrame)
        val isLiquid = TilePump.isLiquid(getWorld.getBlockState(target))
        val isSource = TilePump.isLiquid(getWorld.getBlockState(target), true, getWorld, target)
        if (isLiquid && !isSource) {
          getWorld.setBlockToAir(target)
          nextPos()
        } else if (useEnergy(energy, energy, true, EnergyUsage.ADV_PUMP_FLUID) == energy) {
          val handler = FluidUtil.getFluidHandler(getWorld, target, EnumFacing.UP)
          if (nonNull(handler) && option.get.forall(pos => TilePump.isLiquid(getWorld.getBlockState(pos)))) {
            val drained = handler.drain(Fluid.BUCKET_VOLUME, false)
            FluidHandler.fillInternal(drained, doFill = true)
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
      } else {
        buildWay()
        nextPos()
        break = true
      }
    }
  }

  def push(): Unit = {
    if (delete) {
      FluidHandler.drain(FluidHandler.getAmount, doDrain = true)
      return
    }
    val stack = FluidHandler.drain(FluidHandler.getAmount, doDrain = false)
    if (!stack.isEmpty) {
      for (facing <- EnumFacing.VALUES;
           tile <- Option(getWorld.getTileEntity(getPos.offset(facing)));
           handler <- Option(tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing.getOpposite))) {
        if (stack.amount > 0) {
          val used = handler.fill(stack, true)
          if (used > 0) {
            FluidHandler.drain(stack.copyWithAmount(used), doDrain = true)
            stack.amount -= used
          }
        }
      }
    }
  }

  def start(): Unit = {
    if (!isWorking)
      toStart = true
  }

  def replaceFluidBlock(pos: BlockPos): Unit = {
    FluidUtil.getFluidHandler(getWorld, pos, EnumFacing.UP).drain(Fluid.BUCKET_VOLUME, true)
    if (placeFrame)
      EnumFacing.HORIZONTALS.foreach(facing => {
        val offset = pos.offset(facing)
        if (!inRange.contains(offset) && TilePump.isLiquid(getWorld.getBlockState(offset))) {
          getWorld.setBlockState(offset, QuarryPlusI.blockFrame.getDammingState)
        }
      })
  }

  private def changeState(working: Boolean, state: IBlockState): Unit = {
    if (VersionUtil.changeAdvPumpState()) {
      validate()
      getWorld.setBlockState(getPos, state.withProperty(ADismCBlock.ACTING, Boolean.box(working)))
      validate()
      getWorld.setTileEntity(getPos, this)
    }
  }

  private def findFluid(state: IBlockState) = {
    val block = state.getBlock
    if (block == Blocks.FLOWING_WATER || block == Blocks.WATER) {
      FluidRegistry.WATER
    } else if (block == Blocks.FLOWING_LAVA || block == Blocks.LAVA) {
      FluidRegistry.LAVA
    } else {
      block match {
        case fluidBlock: IFluidBlock => FluidRegistry.getFluid(fluidBlock.getFluid.getName)
        case other => FluidRegistry.lookupFluidForBlock(other)
      }
    }
  }

  /**
    * @return Map (Enchantment id, level)
    */
  override def getEnchantments: util.Map[Integer, Integer] = ench.getMap.collect(enchantCollector).asJava

  /**
    * @param id    Enchantment id
    * @param value level
    */
  override def setEnchantment(id: Short, value: Short): Unit = ench = ench.set(id, value)

  override def getDebugName: String = TranslationKeys.advpump

  override def getDebugMessages: java.util.List[TextComponentString] = {
    List("Range = " + ench.distance,
      "target : " + target,
      "Finished : " + finished,
      "Ench : " + ench,
      "FluidType : " + FluidHandler.getFluidType,
      "FluidAmount : " + FluidHandler.getAmount,
      "Pumped : " + FluidHandler.amountPumped,
      "Delete : " + delete,
      "To Start : " + toStart,
      "Start pos : " + ench.start,
      "End pos : " + ench.end).map(toComponentString).asJava
  }

  //noinspection SpellCheckingInspection
  override def readFromNBT(nbt: NBTTagCompound): Unit = {
    val NBT_POS = "targetpos"
    val NBT_FINISHED = "finished"
    val NBT_PLACE_FRAME = "placeFrame"
    val NBT_DELETE = "delete"
    super.readFromNBT(nbt)
    target = BlockPos.fromLong(nbt.getLong(NBT_POS))
    ench = PEnch.readFromNBT(nbt.getCompoundTag(NBT_PENCH))
    finished = nbt.getBoolean(NBT_FINISHED)
    placeFrame = nbt.getBoolean(NBT_PLACE_FRAME)
    delete = nbt.getBoolean(NBT_DELETE)
    FluidHandler.readFromNBT(nbt.getCompoundTag(NBT_FluidHandler))
  }

  //noinspection SpellCheckingInspection
  override def writeToNBT(nbt: NBTTagCompound): NBTTagCompound = {
    val NBT_POS = "targetpos"
    val NBT_FINISHED = "finished"
    val NBT_PLACE_FRAME = "placeFrame"
    val NBT_DELETE = "delete"
    nbt.setLong(NBT_POS, target.toLong)
    nbt.setBoolean(NBT_FINISHED, finished)
    nbt.setBoolean(NBT_PLACE_FRAME, placeFrame)
    nbt.setBoolean(NBT_DELETE, delete)
    nbt.setTag(NBT_PENCH, ench.toNBT)
    nbt.setTag(NBT_FluidHandler, FluidHandler.toNBT)
    super.writeToNBT(nbt)
  }

  override def hasCapability(capability: Capability[_], facing: EnumFacing): Boolean = {
    if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) true
    else super.hasCapability(capability, facing)
  }

  override def getCapability[T](capability: Capability[T], facing: EnumFacing): T = {
    if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(FluidHandler)
    else super.getCapability(capability, facing)
  }

  private[this] var chunkTicket: ForgeChunkManager.Ticket = _

  override def requestTicket(): Unit = {
    if (this.chunkTicket != null) return
    this.chunkTicket = ForgeChunkManager.requestTicket(QuarryPlus.INSTANCE, getWorld, Type.NORMAL)
    setTileData(this.chunkTicket, getPos)
  }

  override def forceChunkLoading(ticket: ForgeChunkManager.Ticket): Unit = {
    if (this.chunkTicket == null) this.chunkTicket = ticket
    val quarryChunk = new ChunkPos(getPos)
    ForgeChunkManager.forceChunk(ticket, quarryChunk)
  }

  override def onChunkUnload(): Unit = {
    ForgeChunkManager.releaseTicket(this.chunkTicket)
    super.onChunkUnload()
  }

  @SideOnly(Side.CLIENT)
  def receiveStatusMessage(placeFrame: Boolean, nbt: NBTTagCompound): Runnable = new Runnable {
    override def run(): Unit = {
      TileAdvPump.this.placeFrame = placeFrame
      TileAdvPump.this.readFromNBT(nbt)
    }
  }

  def toggleDelete(): Unit = delete = !delete

  private object FluidHandler extends IFluidHandler with INBTWritable with INBTReadable[IFluidHandler] {

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
      nbt.setTag(NBT_liquids, (new NBTTagList).tap(l => fluidStacks.map(_.toNBT).foreach(l.appendTag)))
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
  }

  override protected def getSymbol: Symbol = SYMBOL
}

object TileAdvPump {

  final val SYMBOL = Symbol("AdvancedPump")
  private final val NBT_PENCH = "nbt_pench"
  private final val NBT_FluidHandler = "FluidHandler"
  private final val NBT_pumped = "amountPumped"
  private final val NBT_liquids = "liquds"
  private[this] final val defaultBaseEnergy = Seq(10, 8, 6, 4)
  private[this] final val defaultReceiveEnergy = Seq(32, 64, 128, 256, 512, 1024)
  val defaultEnch = PEnch(efficiency = 0, unbreaking = 0, fortune = 0, silktouch = false, BlockPos.ORIGIN, BlockPos.ORIGIN)

  case class PEnch(efficiency: Int, unbreaking: Int, fortune: Int, silktouch: Boolean, start: BlockPos, end: BlockPos) extends INBTWritable {
    require(efficiency >= 0)
    require(unbreaking >= 0)
    require(fortune >= 0)
    val square: Boolean = start != BlockPos.ORIGIN && end != BlockPos.ORIGIN

    override def writeToNBT(nbt: NBTTagCompound): NBTTagCompound = {
      nbt.setInteger("efficiency", efficiency)
      nbt.setInteger("unbreaking", unbreaking)
      nbt.setInteger("fortune", fortune)
      nbt.setBoolean("silktouch", silktouch)
      nbt.setLong("start", start.toLong)
      nbt.setLong("end", end.toLong)
      nbt
    }

    def getMap: Map[Int, Int] = Map(EfficiencyID -> efficiency, UnbreakingID -> unbreaking,
      FortuneID -> fortune, SilktouchID -> silktouch.compare(false))

    def set(id: Short, level: Int): PEnch = {
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

    val maxAmount: Int = 128 * Fluid.BUCKET_VOLUME * (efficiency + 1)

    def getEnergy(placeFrame: Boolean): Double = {
      defaultBaseEnergy(if (unbreaking >= 3) 3 else unbreaking) * (if (placeFrame) 2.5 else 1)
    }

    def getReceiveEnergy: Double = if (efficiency >= 5) defaultReceiveEnergy(5) else defaultReceiveEnergy(efficiency)

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

  object PEnch extends INBTReadable[PEnch] {
    override def readFromNBT(tag: NBTTagCompound): PEnch = {
      if (!tag.hasNoTags) {
        PEnch(tag.getInteger("efficiency"), tag.getInteger("unbreaking"), tag.getInteger("fortune"), tag.getBoolean("silktouch"),
          BlockPos.fromLong(tag.getLong("start")), BlockPos.fromLong(tag.getLong("end")))
      } else
        PEnch(efficiency = 0, unbreaking = 0, fortune = 0, silktouch = false, BlockPos.ORIGIN, BlockPos.ORIGIN)
    }
  }

  implicit class FluidStackHelper(val fluidStack: FluidStack) extends AnyVal {

    def copyWithAmount(amount: Int): FluidStack = {
      val copied = fluidStack.copy()
      copied.amount = amount
      copied
    }

    def setAmount(amount: Int): FluidStack = {
      fluidStack.amount = amount
      fluidStack
    }

    def isEmpty: Boolean = {
      fluidStack == null || fluidStack.amount <= 0
    }
  }

}
