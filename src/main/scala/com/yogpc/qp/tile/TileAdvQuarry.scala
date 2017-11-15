package com.yogpc.qp.tile

import java.lang.{Boolean => JBool, Byte => JByte, Integer => JInt}

import com.yogpc.qp.block.ADismCBlock
import com.yogpc.qp.compat.{INBTReadable, INBTWritable, InvUtils}
import com.yogpc.qp.tile.TileAdvQuarry.{DigRange, ItemElement, ItemList, QEnch}
import com.yogpc.qp.version.VersionUtil
import com.yogpc.qp.{PowerManager, QuarryPlus, QuarryPlusI, ReflectionHelper}
import net.minecraft.block.properties.PropertyHelper
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.math.{BlockPos, ChunkPos}
import net.minecraft.util.{EnumFacing, ITickable, NonNullList}
import net.minecraftforge.common.ForgeChunkManager
import net.minecraftforge.common.ForgeChunkManager.Type
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.items.{CapabilityItemHandler, IItemHandlerModifiable}

import scala.collection.JavaConverters._

class TileAdvQuarry extends APowerTile with IEnchantableTile with IInventory with ITickable with IDebugSender {
    self =>
    var ench = TileAdvQuarry.defaultEnch
    var digRange = TileAdvQuarry.defaultRange
    var target = BlockPos.ORIGIN
    val cacheItems = new ItemList
    val itemHandler = new ItemHandler
    val mode = new Mode
    val ACTING: PropertyHelper[JBool] = ADismCBlock.ACTING

    override def update() = {
        super.update()
        if (!getWorld.isRemote) {
            if (mode is TileAdvQuarry.MAKEFRAME) {
                def makeFrame(): Unit = {
                    if (target == getPos) {
                        target = nextFrameTarget
                        return
                    } else if (!getWorld.isAirBlock(target)) {
                        val list = NonNullList.create[ItemStack]()
                        val state = getWorld.getBlockState(target)

                        if (ench.silktouch && state.getBlock.canSilkHarvest(getWorld, target, state, null)) {
                            val energy = PowerManager.calcEnergyBreak(self, state.getBlockHardness(getWorld, target), -1, ench.unbreaking)
                            if (useEnergy(energy, energy, false) == energy) {
                                useEnergy(energy, energy, true)
                                list.add(ReflectionHelper.invoke(TileBasic.createStackedBlock, state.getBlock, state).asInstanceOf[ItemStack])
                            }
                        } else {
                            val energy = PowerManager.calcEnergyBreak(self, state.getBlockHardness(getWorld, target), ench.fortune, ench.unbreaking)
                            if (useEnergy(energy, energy, false) == energy) {
                                useEnergy(energy, energy, true)
                                state.getBlock.getDrops(list, getWorld, target, state, ench.fortune)
                            }
                        }
                        list.asScala.foreach(cacheItems.add)
                    }

                    if (PowerManager.useEnergyFrameBuild(self, ench.unbreaking)) {
                        getWorld.setBlockState(target, QuarryPlusI.blockFrame.getDefaultState)
                        target = nextFrameTarget
                    }
                }

                def nextFrameTarget: BlockPos = {
                    ???
                }

                for (_ <- 0 until 4 if mode is TileAdvQuarry.MAKEFRAME)
                    makeFrame()
            } else if (mode is TileAdvQuarry.BREAKBLOCK) {

            } else if (mode is TileAdvQuarry.NOTNEEDBREAK) {

            }
            //TODO quarry action
            if (!isEmpty) {
                var break = false
                var is = cacheItems.list.remove(0).toStack
                while (!break) {
                    val stack = InvUtils.injectToNearTile(getWorld, getPos, is)
                    if (stack.getCount > 0) {
                        cacheItems.add(stack)
                        break = true
                    }
                    if (isEmpty) {
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
        if (!mode.isWorking) {
            this.configure(0, getMaxStored)
        } else if (mode.reduceRecieve) {
            this.configure(ench.maxRecieve / 128, TileAdvQuarry.maxStored)
        } else {
            this.configure(ench.maxRecieve, TileAdvQuarry.maxStored)
        }
        if (!digRange.defined) {
            digRange = makeRangeBox()
            target = new BlockPos(digRange.minX, getPos.getY + 4, digRange.minZ)
        }
    }

    override def readFromNBT(nbttc: NBTTagCompound) = {
        ench = QEnch.readFromNBT(nbttc)
        digRange = DigRange.readFromNBT(nbttc)
        target = BlockPos.fromLong(nbttc.getLong("NBT_TARGET"))
        super.readFromNBT(nbttc)
    }

    override def writeToNBT(nbttc: NBTTagCompound) = {
        ench.writeToNBT(nbttc)
        digRange.writeToNBT(nbttc)
        nbttc.setLong("NBT_TARGET", target.toLong)
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

    override def sendDebugMessage(player: EntityPlayer) = {}

    override def hasCapability(capability: Capability[_], facing: EnumFacing) = {
        capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing)
    }

    override def getCapability[T](capability: Capability[T], facing: EnumFacing) = {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(itemHandler)
        } else
            super.getCapability(capability, facing)
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
              val poses = (m.min(), m.max())
              m.removeFromWorldWithItem().asScala.foreach(cacheItems.add)
              poses
          }.getOrElse({
            val chunkPos = new ChunkPos(getPos)
            val y = getPos.getY
            (new BlockPos(chunkPos.getXStart, y, chunkPos.getZStart), new BlockPos(chunkPos.getZEnd, y, chunkPos.getZEnd))
        })
        new TileAdvQuarry.DigRange(link._1, link._2)
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

    private[TileAdvQuarry] class Mode {

        import TileAdvQuarry._

        private var mode: Modes = NONE

        def set(newmode: Modes): Unit = {
            mode = newmode
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
    }

}

object TileAdvQuarry {

    val maxStored = 30000 * 256
    val defaultEnch = QEnch(efficiency = 0, unbreaking = 0, fortune = 0, silktouch = false)
    val defaultRange: DigRange = NoDefinedRange
    private val ENERGYLIMIT_LIST = IndexedSeq(5120, 10240, 20480, 40960, 81920, maxStored)
    private val NBT_QENCH = "nbt_qench"
    private val NBT_DIGRANGE = "nbt_digrange"

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

    private[TileAdvQuarry] case class DigRange(minX: Double, minY: Double, minZ: Double, maxX: Double, maxY: Double, maxZ: Double) extends INBTWritable {
        def this(minPos: BlockPos, maxPos: BlockPos) {
            this(minPos.getX.toDouble, minPos.getY.toDouble, minPos.getZ.toDouble, maxPos.getX.toDouble, maxPos.getY.toDouble, maxPos.getZ.toDouble)
        }

        val defined = true

        override def writeToNBT(nbt: NBTTagCompound): NBTTagCompound = {
            val t = new NBTTagCompound
            t.setBoolean("defined", defined)
            t.setDouble("minX", minX)
            t.setDouble("minY", minY)
            t.setDouble("minZ", minZ)
            t.setDouble("maxX", maxX)
            t.setDouble("maxY", maxY)
            t.setDouble("maxZ", maxZ)
            nbt.setTag(NBT_DIGRANGE, t)
            nbt
        }
    }

    object DigRange extends INBTReadable[DigRange] {
        override def readFromNBT(tag: NBTTagCompound): DigRange = {
            if (tag.hasKey(NBT_DIGRANGE)) {
                val t = tag.getCompoundTag(NBT_DIGRANGE)
                if (t.getBoolean("defined")) {
                    DigRange(t.getDouble("minX"), t.getDouble("minY"), t.getDouble("minZ"),
                        t.getDouble("maxX"), t.getDouble("maxY"), t.getDouble("maxZ"))
                } else {
                    defaultRange
                }
            } else
                defaultRange
        }
    }

    private object NoDefinedRange extends DigRange(BlockPos.ORIGIN, BlockPos.ORIGIN) {
        override val defined: Boolean = false
    }

    class ItemList {
        val list = scala.collection.mutable.ArrayBuffer.empty[ItemElement]

        def add(itemDamage: ItemDamage, count: Int): Unit = {
            val i = list.indexWhere(_.itemDamage == itemDamage)
            if (i > 0) {
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

    }

    case class ItemElement(itemDamage: ItemDamage, count: Int) {
        def toStack = itemDamage.toStack(count)
    }

    trait Modes

    object NONE extends Modes

    object NOTNEEDBREAK extends Modes

    object MAKEFRAME extends Modes

    object BREAKBLOCK extends Modes

}