package com.yogpc.qp.tile

import java.lang.{Boolean => JBool, Byte => JByte, Integer => JInt}
import java.util.Objects
import javax.annotation.Nonnull

import com.yogpc.qp.block.ADismCBlock
import com.yogpc.qp.compat.{INBTReadable, INBTWritable, InvUtils}
import com.yogpc.qp.packet.PacketHandler
import com.yogpc.qp.packet.advquarry.AdvModeMessage
import com.yogpc.qp.tile.TileAdvQuarry.{DigRange, ItemElement, ItemList, QEnch}
import com.yogpc.qp.version.VersionUtil
import com.yogpc.qp.{PowerManager, QuarryPlus, QuarryPlusI, ReflectionHelper}
import net.minecraft.block.properties.PropertyHelper
import net.minecraft.entity.item.{EntityItem, EntityXPOrb}
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.math.{AxisAlignedBB, BlockPos, ChunkPos}
import net.minecraft.util.text.TextComponentString
import net.minecraft.util.{EnumFacing, ITickable, NonNullList}
import net.minecraft.world.World
import net.minecraftforge.common.ForgeChunkManager
import net.minecraftforge.common.ForgeChunkManager.Type
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.fluids.capability.templates.FluidHandlerFluidMap
import net.minecraftforge.fluids.capability.{FluidTankProperties, IFluidHandler, IFluidTankProperties}
import net.minecraftforge.fluids.{Fluid, FluidStack, FluidTank, FluidUtil}
import net.minecraftforge.fml.relauncher.{Side, SideOnly}
import net.minecraftforge.items.{CapabilityItemHandler, IItemHandlerModifiable}
import net.minecraftforge.oredict.OreDictionary

import scala.collection.JavaConverters._
import scala.collection.convert.WrapAsJava

class TileAdvQuarry extends APowerTile with IEnchantableTile with IInventory with ITickable with IDebugSender {
    self =>
    private var mDigRange = TileAdvQuarry.defaultRange
    var ench = TileAdvQuarry.defaultEnch
    var target = BlockPos.ORIGIN
    var framePoses = List.empty[BlockPos]
    val cacheItems = new ItemList
    val itemHandler = new ItemHandler
    val mode = new Mode
    val ACTING: PropertyHelper[JBool] = ADismCBlock.ACTING
    val fluidStacks = scala.collection.mutable.Map.empty[Fluid, IFluidHandler]

    override def update() = {
        super.update()
        if (!getWorld.isRemote) {
            if (mode is TileAdvQuarry.MAKEFRAME) {
                @inline
                def makeFrame(): Unit = {
                    if (target == getPos) {
                        target = nextFrameTarget
                        return
                    } else if (!getWorld.isAirBlock(target)) {
                        val list = NonNullList.create[ItemStack]()
                        val state = getWorld.getBlockState(target)

                        if (state.getBlock == QuarryPlusI.blockFrame) {
                            target = nextFrameTarget
                            return
                        }

                        if (ench.silktouch && state.getBlock.canSilkHarvest(getWorld, target, state, null)) {
                            val energy = PowerManager.calcEnergyBreak(self, state.getBlockHardness(getWorld, target), -1, ench.unbreaking)
                            if (useEnergy(energy, energy, false) == energy) {
                                useEnergy(energy, energy, true)
                                list.add(ReflectionHelper.invoke(TileBasic.createStackedBlock, state.getBlock, state).asInstanceOf[ItemStack])
                                getWorld.setBlockToAir(target)
                            }
                        } else {
                            val energy = PowerManager.calcEnergyBreak(self, state.getBlockHardness(getWorld, target), ench.fortune, ench.unbreaking)
                            if (useEnergy(energy, energy, false) == energy) {
                                useEnergy(energy, energy, true)
                                state.getBlock.getDrops(list, getWorld, target, state, ench.fortune)
                                getWorld.setBlockToAir(target)
                            }
                        }
                        list.asScala.foreach(cacheItems.add)
                    }

                    if (PowerManager.useEnergyFrameBuild(self, ench.unbreaking)) {
                        getWorld.setBlockState(target, QuarryPlusI.blockFrame.getDefaultState)
                        target = nextFrameTarget
                    }
                }

                @inline
                def nextFrameTarget: BlockPos = {
                    framePoses match {
                        case p :: rest => framePoses = rest; p
                        case Nil => mode set TileAdvQuarry.BREAKBLOCK; new BlockPos(digRange.minX, getPos.getY, digRange.minZ)
                    }
                }

                if (framePoses.isEmpty) {
                    val headtail = TileAdvQuarry.getFramePoses(digRange)
                    target = headtail.head
                    framePoses = headtail.tail
                }
                for (_ <- 0 until 4)
                    if (mode is TileAdvQuarry.MAKEFRAME)
                        makeFrame()
            } else if (mode is TileAdvQuarry.BREAKBLOCK) {
                @inline
                def breakBlocks(): Boolean = {
                    val digPoses = List.iterate(target.down(), target.getY - 1)(_.down())
                    val list = NonNullList.create[ItemStack]()

                    if (target.getX % 3 == 0) {
                        val axis = new AxisAlignedBB(new BlockPos(target.getX - 6, 1, target.getZ - 6), target.add(6, 0, 6))
                        //catch dropped items
                        getWorld.getEntitiesWithinAABB(classOf[EntityItem], axis).asScala.filter(Objects.nonNull).foreach(entity => {
                            if (!entity.isDead) {
                                val drop = entity.getItem
                                if (drop.getCount > 0) {
                                    entity.getEntityWorld.removeEntity(entity)
                                    list.add(drop)
                                }
                            }
                        })
                        //remove XPs
                        getWorld.getEntitiesWithinAABB(classOf[EntityXPOrb], axis).asScala.filter(Objects.nonNull).foreach(entityXPOrb => {
                            if (!entityXPOrb.isDead)
                                entityXPOrb.getEntityWorld.removeEntity(entityXPOrb)
                        })
                    }

                    var destroy, dig, drain = Nil: List[BlockPos]
                    val flags = Array(target.getX == digRange.minX, target.getX == digRange.maxX, target.getZ == digRange.minZ, target.getZ == digRange.maxZ)
                    var requireEnergy = 0d
                    for (pos <- digPoses) {
                        val state = getWorld.getBlockState(pos)
                        if (!state.getBlock.isAir(state, getWorld, pos)) {
                            if (TilePump.isLiquid(state, false, getWorld, pos)) {
                                requireEnergy += PowerManager.calcEnergyPumpDrain(ench.unbreaking, 1, 0)
                                drain = pos :: drain
                            } else {
                                val blockHardness = state.getBlockHardness(getWorld, pos)
                                if (blockHardness != -1 && !blockHardness.isInfinity) {
                                    if (TileAdvQuarry.noDigBLOCKS.contains(ItemDamage(state.getBlock))) {
                                        requireEnergy += PowerManager.calcEnergyBreak(this, blockHardness, 0, ench.unbreaking)
                                        destroy = pos :: destroy
                                    } else {
                                        requireEnergy += PowerManager.calcEnergyBreak(this, blockHardness, ench.mode, ench.unbreaking)
                                        dig = pos :: dig
                                    }
                                } else if ((state.getBlock == Blocks.BEDROCK) && ((pos.getY > 0 && pos.getY <= 5) || (pos.getY > 122 && pos.getY < 127))) {
                                    requireEnergy += 200
                                    destroy = pos :: destroy
                                } else if (state.getBlock == Blocks.PORTAL) {
                                    getWorld.setBlockToAir(pos)
                                    requireEnergy += 200
                                }
                            }

                            def checkandsetFrame(world: World, thatPos: BlockPos): Unit = {
                                if (TilePump.isLiquid(world.getBlockState(thatPos), false, world, thatPos)) {
                                    world.setBlockState(thatPos, QuarryPlusI.blockFrame.getDamiingState)
                                }
                            }

                            if (flags(0)) { //-x
                                checkandsetFrame(getWorld, pos.offset(EnumFacing.WEST))
                                if (flags(2)) { //-z, -x
                                    checkandsetFrame(getWorld, pos.offset(EnumFacing.NORTH).offset(EnumFacing.WEST))
                                }
                                else if (flags(3)) { //+z, -x
                                    checkandsetFrame(getWorld, pos.offset(EnumFacing.SOUTH).offset(EnumFacing.WEST))
                                }
                            }
                            else if (flags(1)) { //+x
                                checkandsetFrame(getWorld, pos.offset(EnumFacing.EAST))
                                if (flags(2)) { //-z, +x
                                    checkandsetFrame(getWorld, pos.offset(EnumFacing.NORTH).offset(EnumFacing.EAST))
                                }
                                else if (flags(3)) { //+z, +x
                                    checkandsetFrame(getWorld, pos.offset(EnumFacing.SOUTH).offset(EnumFacing.EAST))
                                }
                            }
                            if (flags(2)) { //-z
                                checkandsetFrame(getWorld, pos.offset(EnumFacing.NORTH))
                            }
                            else if (flags(3)) { //+z
                                checkandsetFrame(getWorld, pos.offset(EnumFacing.SOUTH))
                            }
                        }
                    }

                    if (useEnergy(requireEnergy, requireEnergy, false) == requireEnergy) {
                        useEnergy(requireEnergy, requireEnergy, true)
                        dig.foreach(p => {
                            val state = getWorld.getBlockState(p)
                            if (ench.silktouch && state.getBlock.canSilkHarvest(getWorld, p, state, null)) {
                                list.add(ReflectionHelper.invoke(TileBasic.createStackedBlock, state.getBlock, state).asInstanceOf[ItemStack])
                            } else {
                                state.getBlock.getDrops(list, getWorld, p, state, ench.fortune)
                            }
                            getWorld.setBlockState(p, Blocks.AIR.getDefaultState, 2)
                        })
                        destroy.foreach(getWorld.setBlockState(_, Blocks.AIR.getDefaultState, 2))
                        drain.foreach(p => {
                            val handler = Option(FluidUtil.getFluidHandler(getWorld, p, EnumFacing.UP))
                            val fluidOp = handler.map(_.getTankProperties.apply(0)).flatMap(p => Option(p.getContents)).map(_.getFluid)
                            fluidOp match {
                                case Some(fluid) => handler.map(_.drain(Fluid.BUCKET_VOLUME, false)).foreach(s => fluidStacks.get(fluid) match {
                                    case Some(tank) => tank.fill(s, true)
                                    case None => fluidStacks.put(fluid, new FluidTank(s, Int.MaxValue))
                                })
                                case None => //QuarryPlus.LOGGER.error(s"Adv Fluid null, ${getWorld.getBlockState(p)}, ${FluidUtil.getFluidHandler(getWorld, p, EnumFacing.UP)}")
                            }

                            getWorld.setBlockState(p, Blocks.AIR.getDefaultState, 2)
                        })
                        list.asScala.foreach(cacheItems.add)
                        true
                    } else {
                        false
                    }
                }

                if (breakBlocks()) {
                    val x = target.getX + 1
                    if (x > digRange.maxX) {
                        val z = target.getZ + 1
                        if (z > digRange.maxZ) {
                            //Finished.
                            target = BlockPos.ORIGIN
                            mode set TileAdvQuarry.NONE
                            digRange = TileAdvQuarry.defaultRange
                        } else {
                            target = new BlockPos(digRange.minX, target.getY, z)
                        }
                    } else {
                        target = new BlockPos(x, target.getY, target.getZ)
                    }
                }

            } else if (mode is TileAdvQuarry.NOTNEEDBREAK) {
                if (digRange.defined)
                    if (getStoredEnergy > getMaxStored * 0.3)
                        mode set TileAdvQuarry.MAKEFRAME
            }
            if (!isEmpty) {
                var break = false
                var is = cacheItems.list.remove(0).toStack
                while (!break) {
                    val stack = InvUtils.injectToNearTile(getWorld, getPos, is)
                    if (stack.getCount > 0) {
                        cacheItems.add(stack)
                        break = true
                    }
                    if (isEmpty || break) {
                        break = true
                    } else {
                        is = cacheItems.list.remove(0).toStack
                    }
                }
            }
        }
    }

    override protected def isWorking = mode.isWorking

    override def G_reinit(): Unit = {
        mode.set(TileAdvQuarry.NOTNEEDBREAK)
        if (!digRange.defined) {
            digRange = makeRangeBox()
        }
    }

    def energyConfigure(): Unit = {
        if (!mode.isWorking) {
            this.configure(0, getMaxStored)
        } else if (mode.reduceRecieve) {
            this.configure(ench.maxRecieve / 128, TileAdvQuarry.MAX_STORED)
        } else {
            this.configure(ench.maxRecieve, TileAdvQuarry.MAX_STORED)
        }
    }

    override def readFromNBT(nbttc: NBTTagCompound) = {
        ench = QEnch.readFromNBT(nbttc)
        digRange = DigRange.readFromNBT(nbttc)
        target = BlockPos.fromLong(nbttc.getLong("NBT_TARGET"))
        mode.readFromNBT(nbttc)
        super.readFromNBT(nbttc)
    }

    override def writeToNBT(nbttc: NBTTagCompound) = {
        ench.writeToNBT(nbttc)
        digRange.writeToNBT(nbttc)
        nbttc.setLong("NBT_TARGET", target.toLong)
        mode.writeToNBT(nbttc)
        super.writeToNBT(nbttc)
    }

    /**
      * @return Map (Enchantment id, level)
      */
    override def getEnchantments = ench.getMap.map { case (a, b) => (JInt.valueOf(a), JByte.valueOf(b)) }.asJava

    /**
      * @param id    Enchantment id
      * @param value level
      */
    override def setEnchantent(id: Short, value: Short) = ench = ench.set(id, value)

    override def getField(id: Int) = 0

    override def getFieldCount = 0

    override def setField(id: Int, value: Int) = ()

    override def isItemValidForSlot(index: Int, stack: ItemStack) = false

    override def openInventory(player: EntityPlayer) = ()

    override def closeInventory(player: EntityPlayer) = ()

    override def setInventorySlotContents(index: Int, stack: ItemStack) = {
        if (VersionUtil.nonEmpty(stack)) {
            QuarryPlus.LOGGER.warn("QuarryPlus WARN: call setInventorySlotContents with non empty ItemStack.")
        } else {
            removeStackFromSlot(index)
        }
    }

    override def decrStackSize(index: Int, count: Int) = cacheItems.decrease(index, count)

    override def getSizeInventory = cacheItems.list.size

    override def removeStackFromSlot(index: Int) = cacheItems.list.remove(index).toStack

    override def isUsableByPlayer(player: EntityPlayer) = getWorld.getTileEntity(getPos) eq this

    override def getInventoryStackLimit = 1

    override def clear() = cacheItems.list.clear()

    override def isEmpty = cacheItems.list.isEmpty

    override def getStackInSlot(index: Int) = cacheItems.list(index).toStack

    override def hasCustomName = false

    override def getName = "tile.chunkdestroyer.name"

    override def sendDebugMessage(player: EntityPlayer) = {
        player.sendStatusMessage(new TextComponentString("Items to extract = " + cacheItems.list.size), false)
        player.sendStatusMessage(new TextComponentString("Liquid to extract = " + fluidStacks.size), false)
        player.sendStatusMessage(new TextComponentString("Next target = " + target.toString), false)
        player.sendStatusMessage(new TextComponentString("Now mode = " + mode), false)
        player.sendStatusMessage(new TextComponentString("Dig range = " + digRange), false)
    }

    override def hasCapability(capability: Capability[_], facing: EnumFacing) = {
        capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing)
    }

    override def getCapability[T](capability: Capability[T], facing: EnumFacing) = {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(itemHandler)
        } else
            super.getCapability(capability, facing)
    }

    override def hasFastRenderer: Boolean = true

    override def getRenderBoundingBox: AxisAlignedBB = {
        if (digRange.defined) {
            new AxisAlignedBB(digRange.minX, digRange.minY, digRange.minZ, digRange.maxX, digRange.maxY, digRange.maxZ)
        } else
            super.getRenderBoundingBox
    }

    override def onLoad(): Unit = {
        super.onLoad()
        energyConfigure()
    }

    private var chunkTicket: ForgeChunkManager.Ticket = _

    def requestTicket(): Unit = {
        if (this.chunkTicket != null) return
        this.chunkTicket = ForgeChunkManager.requestTicket(QuarryPlus.INSTANCE, getWorld, Type.NORMAL)
        if (this.chunkTicket == null) return
        val tag: NBTTagCompound = this.chunkTicket.getModData
        tag.setInteger("quarryX", getPos.getX)
        tag.setInteger("quarryY", getPos.getY)
        tag.setInteger("quarryZ", getPos.getZ)
        forceChunkLoading(this.chunkTicket)
    }

    def forceChunkLoading(ticket: ForgeChunkManager.Ticket): Unit = {
        if (this.chunkTicket == null) this.chunkTicket = ticket
        val quarryChunk: ChunkPos = new ChunkPos(getPos)
        ForgeChunkManager.forceChunk(ticket, quarryChunk)
    }

    def makeRangeBox() = {
        val facing = getWorld.getBlockState(getPos).getValue(ADismCBlock.FACING).getOpposite
        val link = List(getPos.offset(facing), getPos.offset(facing.rotateYCCW), getPos.offset(facing.rotateY)).map(getWorld.getTileEntity(_))
          .collectFirst { case m: TileMarker if m.link != null =>
              val poses = (m.min().add(+1, 0, +1), m.max().add(-1, 0, -1))
              m.removeFromWorldWithItem().asScala.foreach(cacheItems.add)
              poses
          }.getOrElse({
            val chunkPos = new ChunkPos(getPos)
            val y = getPos.getY
            (new BlockPos(chunkPos.getXStart, y, chunkPos.getZStart), new BlockPos(chunkPos.getXEnd, y, chunkPos.getZEnd))
        })
        new TileAdvQuarry.DigRange(link._1, link._2)
    }

    def digRange = mDigRange

    def digRange_=(@Nonnull digRange: TileAdvQuarry.DigRange): Unit = {
        require(digRange != null, "DigRange must not be null.")
        mDigRange = digRange
    }

    @SideOnly(Side.CLIENT)
    def recieveModeMassage(modeTag: NBTTagCompound): Unit = {
        mode.readFromNBT(modeTag)
        digRange = DigRange.readFromNBT(modeTag)
    }

    private[TileAdvQuarry] class ItemHandler extends IItemHandlerModifiable {
        override def setStackInSlot(slot: Int, stack: ItemStack): Unit = self.setInventorySlotContents(slot, stack)

        override def getStackInSlot(slot: Int): ItemStack = self.getStackInSlot(slot)

        override def extractItem(slot: Int, amount: Int, simulate: Boolean): ItemStack = {
            if (simulate) {
                cacheItems.list(slot) match {
                    case ItemElement(i, size) => i.toStack(Math.min(amount, Math.min(size, i.itemStackLimit)))
                }
            } else {
                self.decrStackSize(slot, amount)
            }
        }

        override def getSlotLimit(slot: Int): Int = 1

        override def getSlots: Int = self.getSizeInventory

        override def insertItem(slot: Int, stack: ItemStack, simulate: Boolean): ItemStack = stack
    }

    private[TileAdvQuarry] class FluidHandler extends FluidHandlerFluidMap(WrapAsJava.mutableMapAsJavaMap(fluidStacks)) {
        val emptyProperty = new FluidTankProperties(null, 0, false, false)

        /**
          * Not fillable.
          */
        override def fill(resource: FluidStack, doFill: Boolean): Int = 0

        override def getTankProperties: Array[IFluidTankProperties] = {
            if (fluidStacks.nonEmpty) {
                super.getTankProperties
            } else {
                Array(emptyProperty)
            }
        }
    }

    private[TileAdvQuarry] class Mode extends INBTWritable with INBTReadable[Mode] {

        import TileAdvQuarry._

        private var mode: Modes = NONE

        def set(newmode: Modes): Unit = {
            mode = newmode
            if (!getWorld.isRemote) {
                energyConfigure()
                PacketHandler.sendToAround(AdvModeMessage.create(self), getWorld, getPos)
            }
            val state = getWorld.getBlockState(getPos)
            if (state.getValue(ACTING)) {
                if (newmode == NONE) {
                    validate()
                    getWorld.setBlockState(getPos, state.withProperty(ACTING, JBool.FALSE))
                    validate()
                    getWorld.setTileEntity(getPos, self)
                }
            } else {
                if (newmode != NONE) {
                    validate()
                    getWorld.setBlockState(getPos, state.withProperty(ACTING, JBool.TRUE))
                    validate()
                    getWorld.setTileEntity(getPos, self)
                }
            }
        }

        def is(modes: Modes): Boolean = mode == modes

        def isWorking = !is(NONE)

        def reduceRecieve = is(MAKEFRAME)

        override def toString: String = "ChunkDestroyer mode = " + mode

        override def writeToNBT(nbt: NBTTagCompound): NBTTagCompound = {
            val tag = new NBTTagCompound
            tag.setInteger("mode", mode.index)
            nbt.setTag(NBT_MODE, tag)
            nbt
        }

        override def readFromNBT(tag: NBTTagCompound): Mode = {
            if (tag hasKey NBT_MODE) {
                val t = tag.getCompoundTag(NBT_MODE)
                this.mode = t.getInteger("mode") match {
                    case 0 => NONE
                    case 1 => NOTNEEDBREAK
                    case 2 => MAKEFRAME
                    case 3 => BREAKBLOCK
                    case _ => throw new RuntimeException("No available mode")
                }
            }
            this
        }
    }

}

object TileAdvQuarry {

    val MAX_STORED = 3000 * 256
    val noDigBLOCKS = Set(
        ItemDamage(Blocks.STONE, OreDictionary.WILDCARD_VALUE),
        ItemDamage(Blocks.COBBLESTONE),
        ItemDamage(Blocks.DIRT),
        ItemDamage(Blocks.GRASS),
        ItemDamage(Blocks.NETHERRACK),
        ItemDamage(Blocks.SANDSTONE, OreDictionary.WILDCARD_VALUE),
        ItemDamage(Blocks.RED_SANDSTONE, OreDictionary.WILDCARD_VALUE))
    private val ENERGYLIMIT_LIST = IndexedSeq(5120, 10240, 20480, 40960, 81920, MAX_STORED)
    private val NBT_QENCH = "nbt_qench"
    private val NBT_DIGRANGE = "nbt_digrange"
    private val NBT_MODE = "nbt_quarrymode"

    val defaultEnch = QEnch(efficiency = 0, unbreaking = 0, fortune = 0, silktouch = false)
    val defaultRange: DigRange = NoDefinedRange

    private[TileAdvQuarry] case class QEnch(efficiency: Byte, unbreaking: Byte, fortune: Byte, silktouch: Boolean) extends INBTWritable {

        require(efficiency >= 0 && unbreaking >= 0 && fortune >= 0, "Chunk Destroyer Enchantment error")

        import IEnchantableTile._

        def set(id: Short, level: Int): QEnch = {
            id match {
                case EfficiencyID => this.copy(efficiency = level.toByte)
                case UnbreakingID => this.copy(unbreaking = level.toByte)
                case FortuneID => this.copy(fortune = level.toByte)
                case SilktouchID => this.copy(silktouch = level > 0)
                case _ => this
            }
        }

        def getMap = Map(EfficiencyID -> efficiency, UnbreakingID -> unbreaking,
            FortuneID -> fortune, SilktouchID -> silktouch.compare(false).toByte)

        val maxRecieve = if (efficiency >= 5) ENERGYLIMIT_LIST(5) else ENERGYLIMIT_LIST(efficiency)

        val mode: Byte = if (silktouch) -1 else fortune

        override def writeToNBT(nbt: NBTTagCompound): NBTTagCompound = {
            val t = new NBTTagCompound
            t.setByte("efficiency", efficiency)
            t.setByte("unbreaking", unbreaking)
            t.setByte("fortune", fortune)
            t.setBoolean("silktouch", silktouch)
            nbt.setTag(NBT_QENCH, t)
            nbt
        }
    }

    object QEnch extends INBTReadable[QEnch] {
        override def readFromNBT(tag: NBTTagCompound): QEnch = {
            if (tag.hasKey(NBT_QENCH)) {
                val t = tag.getCompoundTag(NBT_QENCH)
                QEnch(t.getByte("efficiency"), t.getByte("unbreaking"), t.getByte("fortune"), t.getBoolean("silktouch"))
            } else
                defaultEnch
        }
    }

    case class DigRange(minX: Int, minY: Int, minZ: Int, maxX: Int, maxY: Int, maxZ: Int) extends INBTWritable {
        def this(minPos: BlockPos, maxPos: BlockPos) {
            this(minPos.getX, minPos.getY, minPos.getZ, maxPos.getX, maxPos.getY, maxPos.getZ)
        }

        val defined = true

        override val toString: String = s"Dig Range from ($minX, $minY, $minZ) to ($maxX, $maxY, $maxZ)"

        override def writeToNBT(nbt: NBTTagCompound): NBTTagCompound = {
            val t = new NBTTagCompound
            t.setBoolean("defined", defined)
            t.setInteger("minX", minX)
            t.setInteger("minY", minY)
            t.setInteger("minZ", minZ)
            t.setInteger("maxX", maxX)
            t.setInteger("maxY", maxY)
            t.setInteger("maxZ", maxZ)
            nbt.setTag(NBT_DIGRANGE, t)
            nbt
        }
    }

    object DigRange extends INBTReadable[DigRange] {
        override def readFromNBT(tag: NBTTagCompound): DigRange = {
            if (tag.hasKey(NBT_DIGRANGE)) {
                val t = tag.getCompoundTag(NBT_DIGRANGE)
                if (t.getBoolean("defined")) {
                    DigRange(t.getInteger("minX"), t.getInteger("minY"), t.getInteger("minZ"),
                        t.getInteger("maxX"), t.getInteger("maxY"), t.getInteger("maxZ"))
                } else {
                    defaultRange
                }
            } else
                defaultRange
        }
    }

    private object NoDefinedRange extends DigRange(BlockPos.ORIGIN, BlockPos.ORIGIN) {
        override val defined: Boolean = false

        override val toString: String = "Dig Range Not Defined"
    }

    class ItemList {
        val list = scala.collection.mutable.ArrayBuffer.empty[ItemElement]

        def add(itemDamage: ItemDamage, count: Int): Unit = {
            val i = list.indexWhere(_.itemDamage == itemDamage)
            if (i > -1) {
                val e = list(i).count
                val newCount = e + count
                if (newCount > 0) {
                    list.update(i, ItemElement(itemDamage, newCount))
                } else {
                    list.remove(i)
                }
            } else {
                if (count > 0)
                    list += ItemElement(itemDamage, count)
            }
        }

        def add(stack: ItemStack): Unit = {
            add(ItemDamage(stack), stack.getCount)
        }

        def decrease(index: Int, count: Int): ItemStack = {
            val t = list(index)
            val min = Math.min(count, t.itemDamage.itemStackLimit)
            if (t.count <= min) {
                list.remove(index)
                t.itemDamage.toStack(t.count)
            } else {
                list(index) = ItemElement(t.itemDamage, t.count - min)
                t.itemDamage.toStack(min)
            }
        }

        override def toString: String = "ItemList size = " + list.size
    }

    case class ItemElement(itemDamage: ItemDamage, count: Int) {
        def toStack = itemDamage.toStack(count)

        override def toString: String = itemDamage.toString + "x" + count
    }

    sealed class Modes(val index: Int)

    case object NONE extends Modes(0)

    case object NOTNEEDBREAK extends Modes(1)

    case object MAKEFRAME extends Modes(2)

    case object BREAKBLOCK extends Modes(3)

    def getFramePoses(digRange: DigRange): List[BlockPos] = {
        val builder = List.newBuilder[BlockPos]
        for (i <- 0 to 4) {
            builder += new BlockPos(digRange.minX - 1, digRange.maxY + 4 - i, digRange.minZ - 1)
            builder += new BlockPos(digRange.minX - 1, digRange.maxY + 4 - i, digRange.maxZ + 1)
            builder += new BlockPos(digRange.maxX + 1, digRange.maxY + 4 - i, digRange.maxZ + 1)
            builder += new BlockPos(digRange.maxX + 1, digRange.maxY + 4 - i, digRange.minZ - 1)
        }
        for (x <- digRange.minX to digRange.maxX) {
            builder += new BlockPos(x, digRange.maxY + 4, digRange.minZ - 1)
            builder += new BlockPos(x, digRange.maxY + 0, digRange.minZ - 1)
            builder += new BlockPos(x, digRange.maxY + 0, digRange.maxZ + 1)
            builder += new BlockPos(x, digRange.maxY + 4, digRange.maxZ + 1)
        }
        for (z <- digRange.minZ to digRange.maxZ) {
            builder += new BlockPos(digRange.minX - 1, digRange.maxY + 4, z)
            builder += new BlockPos(digRange.minX - 1, digRange.maxY + 0, z)
            builder += new BlockPos(digRange.maxX + 1, digRange.maxY + 0, z)
            builder += new BlockPos(digRange.maxX + 1, digRange.maxY + 4, z)
        }
        builder.result()
    }
}