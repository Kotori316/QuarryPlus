package com.yogpc.qp.tile

import java.lang.{Byte => JByte, Integer => JInt}

import com.yogpc.qp.QuarryPlus
import com.yogpc.qp.compat.{INBTReadable, INBTWritable}
import com.yogpc.qp.tile.IEnchantableTile.{EfficiencyID, FortuneID, SilktouchID, UnbreakingID}
import com.yogpc.qp.tile.TileAdvPump._
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.math.{BlockPos, ChunkPos}
import net.minecraft.util.{EnumFacing, ITickable}
import net.minecraftforge.common.ForgeChunkManager
import net.minecraftforge.common.ForgeChunkManager.Type
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.fluids.capability.{CapabilityFluidHandler, FluidTankProperties, IFluidHandler, IFluidTankProperties}
import net.minecraftforge.fluids.{Fluid, FluidStack}

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

/**
  * @see [[buildcraft.factory.tile.TilePump]]
  */
class TileAdvPump extends APowerTile with IEnchantableTile with ITickable with IDebugSender {

    private[this] var ench = TileAdvPump.defaultEnch
    var target = BlockPos.ORIGIN
    val distance = 64

    override protected def isWorking = false

    override def G_reinit() = {
        configure(128d, 1024d)
    }

    override def update() = {
        super.update()
    }

    /**
      * @return Map (Enchantment id, level)
      */
    override def getEnchantments = ench.getMap.collect { case (a, b) if b > 0 => (JInt.valueOf(a), JByte.valueOf(b)) }.asJava

    /**
      * @param id    Enchantment id
      * @param value level
      */
    override def setEnchantent(id: Short, value: Short) = ench = ench.set(id, value)

    override def getDebugName = "tile.standalonepump.name"

    override def getDebugmessages = {
        java.util.Collections.emptyList()
    }

    override def hasCapability(capability: Capability[_], facing: EnumFacing) = {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) true
        else super.hasCapability(capability, facing)
    }

    override def getCapability[T](capability: Capability[T], facing: EnumFacing) = {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(FluidHandler)
        else super.getCapability(capability, facing)
    }

    override def onLoad(): Unit = super.onLoad()

    private[this] var chunkTicket: ForgeChunkManager.Ticket = _

    def requestTicket(): Unit = {
        if (this.chunkTicket != null) return
        this.chunkTicket = ForgeChunkManager.requestTicket(QuarryPlus.INSTANCE, getWorld, Type.NORMAL)
        if (this.chunkTicket == null) return
        val tag = this.chunkTicket.getModData
        tag.setInteger("quarryX", getPos.getX)
        tag.setInteger("quarryY", getPos.getY)
        tag.setInteger("quarryZ", getPos.getZ)
        forceChunkLoading(this.chunkTicket)
    }

    def forceChunkLoading(ticket: ForgeChunkManager.Ticket): Unit = {
        if (this.chunkTicket == null) this.chunkTicket = ticket
        val quarryChunk = new ChunkPos(getPos)
        ForgeChunkManager.forceChunk(ticket, quarryChunk)
    }

    override def onChunkUnload(): Unit = {
        ForgeChunkManager.releaseTicket(this.chunkTicket)
        super.onChunkUnload()
    }

    private object FluidHandler extends IFluidHandler {

        private[this] val fluidStacks = new ListBuffer[FluidStack]

        override def fill(resource: FluidStack, doFill: Boolean): Int = {
            fluidStacks.find(_ == resource) match {
                case Some(stack) =>
                    val nAmount = stack.amount + resource.amount
                    if (nAmount <= maxAmount) {
                        if (doFill) stack.amount = nAmount
                        resource.amount
                    } else {
                        if (doFill) stack.amount = maxAmount
                        nAmount - stack.amount
                    }
                case None => if (doFill) fluidStacks += resource.copy(); resource.amount
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
                case Some(stack) => drainInternal(stack.copywithAmount(maxDrain), stack, doDrain)
            }
        }

        private def drainInternal(kind: FluidStack, source: FluidStack, doDrain: Boolean): FluidStack = {
            if (kind.amount <= 0) {
                return null
            }
            if (kind.amount >= source.amount) {
                val extract = source.amount
                if (doDrain) fluidStacks.remove(fluidStacks.indexOf(kind))
                kind.setAmount(extract)
                kind
            } else {
                val nAmount = source.amount - kind.amount
                if (doDrain) source.setAmount(nAmount)
                kind
            }
        }

        override def getTankProperties: Array[IFluidTankProperties] = {
            if (fluidStacks.isEmpty) {
                Array(new FluidTankProperties(null, 0, false, false))
            } else {
                fluidStacks.map(s => new FluidTankProperties(s, s.amount, false, true)).toArray
            }
        }
    }

}

object TileAdvPump {

    val maxAmount = 128 * Fluid.BUCKET_VOLUME
    private val NBT_PENCH = "nbt_pench"
    val defaultEnch = PEnch(efficiency = 0, unbreaking = 0, fortune = 0, silktouch = false)

    case class PEnch(efficiency: Byte, unbreaking: Byte, fortune: Byte, silktouch: Boolean) extends INBTWritable {
        override def writeToNBT(nbt: NBTTagCompound): NBTTagCompound = {
            val t = new NBTTagCompound
            t.setByte("efficiency", efficiency)
            t.setByte("unbreaking", unbreaking)
            t.setByte("fortune", fortune)
            t.setBoolean("silktouch", silktouch)
            nbt.setTag(NBT_PENCH, t)
            nbt
        }

        def getMap = Map(EfficiencyID -> efficiency, UnbreakingID -> unbreaking,
            FortuneID -> fortune, SilktouchID -> silktouch.compare(false).toByte)

        def set(id: Short, level: Int): PEnch = {
            id match {
                case EfficiencyID => this.copy(efficiency = level.toByte)
                case UnbreakingID => this.copy(unbreaking = level.toByte)
                case FortuneID => this.copy(fortune = level.toByte)
                case SilktouchID => this.copy(silktouch = level > 0)
                case _ => this
            }
        }
    }

    object PEnch extends INBTReadable[PEnch] {
        override def readFromNBT(tag: NBTTagCompound): PEnch = {
            if (tag.hasKey(NBT_PENCH)) {
                val t = tag.getCompoundTag(NBT_PENCH)
                PEnch(t.getByte("efficiency"), t.getByte("unbreaking"), t.getByte("fortune"), t.getBoolean("silktouch"))
            } else
                PEnch(efficiency = 0, unbreaking = 0, fortune = 0, silktouch = false)
        }
    }

    implicit class FluidStackHelper(val fluidStack: FluidStack) extends AnyVal {

        def copywithAmount(amount: Int): FluidStack = {
            val copied = fluidStack.copy()
            copied.amount = amount
            copied
        }

        def setAmount(amount: Int): Unit = {
            fluidStack.amount = amount
        }
    }

}