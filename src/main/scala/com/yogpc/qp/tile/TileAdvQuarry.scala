package com.yogpc.qp.tile

import com.yogpc.qp.QuarryPlus
import com.yogpc.qp.block.ADismCBlock
import com.yogpc.qp.tile.TileAdvQuarry.ItemList
import com.yogpc.qp.version.VersionUtil
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.inventory.IInventory
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.math.ChunkPos
import net.minecraftforge.common.ForgeChunkManager
import net.minecraftforge.common.ForgeChunkManager.Type
import net.minecraftforge.items.IItemHandlerModifiable

import scala.collection.JavaConverters._

class TileAdvQuarry extends APowerTile with IEnchantableTile with IInventory {
    self =>
    var ench = TileAdvQuarry.defaultEnch
    val cacheItems = new ItemList
    val itemHandler = new ItemHandler
    val mode = new Mode

    override def update() = {
        super.update()
    }

    override protected def isWorking = mode.isWorking

    override def G_reinit(): Unit = {
        mode.set(TileAdvQuarry.NOTNEEDBREAK)

    }

    /**
      * @return Map (Enchantment id, level)
      */
    override def getEnchantments = ench.getMap.map { case (a, b) => (java.lang.Integer.valueOf(a), java.lang.Byte.valueOf(b)) }.asJava

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

    override def removeStackFromSlot(index: Int) =
        cacheItems.list.remove(index) match {
            case (i, size) => i.toStack(size)
        }

    override def isUsableByPlayer(player: EntityPlayer) = getWorld.getTileEntity(getPos) eq this

    override def getInventoryStackLimit = 1

    override def clear() = cacheItems.list.clear()

    override def isEmpty = cacheItems.list.isEmpty

    override def getStackInSlot(index: Int) = {
        cacheItems.list(index) match {
            case (i, size) => i.toStack(size)
        }
    }

    override def hasCustomName = false

    override def getName = "tile.chunkdestroyer.name"

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

    private class ItemHandler extends IItemHandlerModifiable {
        override def setStackInSlot(slot: Int, stack: ItemStack): Unit = self.setInventorySlotContents(slot, stack)

        override def getStackInSlot(slot: Int): ItemStack = self.getStackInSlot(slot)

        override def extractItem(slot: Int, amount: Int, simulate: Boolean): ItemStack = {
            if (simulate) {
                cacheItems.list(slot) match {
                    case (i, size) => i.toStack(Math.min(amount, Math.min(size, i.itemStackLimit)))
                }
            } else {
                self.decrStackSize(slot, amount)
            }
        }

        override def getSlotLimit(slot: Int): Int = 1

        override def getSlots: Int = self.getSizeInventory

        override def insertItem(slot: Int, stack: ItemStack, simulate: Boolean): ItemStack = stack
    }

    private class Mode {

        import TileAdvQuarry._

        private var mode: Modes = NONE

        def set(newmode: Modes): Unit = {
            mode = newmode
            val state = getWorld.getBlockState(getPos)
            if (state.getValue(ADismCBlock.ACTING)) {
                if (newmode == NONE) {
                    validate()
                    getWorld.setBlockState(getPos, state.withProperty(ADismCBlock.ACTING, false))
                    validate()
                    getWorld.setTileEntity(getPos, self)
                }
            } else {
                if (newmode != NONE) {
                    validate()
                    getWorld.setBlockState(getPos, state.withProperty(ADismCBlock.ACTING, true))
                    validate()
                    getWorld.setTileEntity(getPos, self)
                }
            }
        }

        def isWorking = mode != NONE
    }

}

object TileAdvQuarry {

    val defaultEnch = QEnch(efficiency = 0, unbreaking = 0, fortune = 0, silktouch = false)

    private case class QEnch(efficiency: Byte, unbreaking: Byte, fortune: Byte, silktouch: Boolean) {

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
    }

    private class ItemList {
        val list = scala.collection.mutable.ArrayBuffer.empty[(ItemDamage, Int)]

        def add(itemDamage: ItemDamage, count: Int): Unit = {
            val i = list.indexWhere(_._1 == itemDamage)
            if (i > 0) {
                val e = list(i)._2
                val newCount = e + count
                if (newCount > 0) {
                    list.update(i, (itemDamage, newCount))
                } else {
                    list.remove(i)
                }
            } else {
                if (count > 0)
                    list += ((itemDamage, count))
            }
        }

        def decrease(index: Int, count: Int): ItemStack = {
            val t = list(index)
            val min = Math.min(count, t._1.itemStackLimit)
            if (t._2 <= min) {
                list.remove(index)
                t._1.toStack(t._2)
            } else {
                list(index) = (t._1, t._2 - min)
                t._1.toStack(min)
            }
        }

    }

    trait Modes

    object NONE extends Modes

    object NOTNEEDBREAK extends Modes

    object MAKEFRAME extends Modes

    object BREAKBLOCK extends Modes

}