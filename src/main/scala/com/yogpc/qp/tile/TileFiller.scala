package com.yogpc.qp.tile

import com.yogpc.qp._
import com.yogpc.qp.gui.TranslationKeys
import net.minecraft.inventory.InventoryBasic
import net.minecraft.util.math.ChunkPos
import net.minecraft.util.{EnumFacing, ITickable}
import net.minecraftforge.common.ForgeChunkManager
import net.minecraftforge.common.ForgeChunkManager.Type
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.items.CapabilityItemHandler
import net.minecraftforge.items.wrapper.InvWrapper

import scala.collection.JavaConverters._

final class TileFiller
  extends APowerTile
    with ITickable
    with IDebugSender
    with IChunkLoadTile {
  val inventory = new InventoryBasic(TranslationKeys.filler, false, TileFiller.slotCount)
  var work: TileFiller.Work = TileFiller.Wait

  // TileEntity Overrides

  override def hasCapability(capability: Capability[_], facing: EnumFacing) =
    capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing)

  override def getCapability[T](capability: Capability[T], facing: EnumFacing): T = {
    if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
      CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(new InvWrapper(inventory))
    else
      super.getCapability(capability, facing)
  }

  override def onLoad(): Unit = {
    super.onLoad()
    configure(5000 * APowerTile.MJToMicroMJ, 5000 * APowerTile.MJToMicroMJ)
  }

  // Implemented methods

  override protected def isWorking: Boolean = this.work.working

  override protected def getSymbol: Symbol = TileFiller.SYMBOL

  override def getDebugName: String = TranslationKeys.filler

  override def getDebugMessages = Seq(
    "Work: " + this.work
  ).map(toComponentString).asJava

  // Chunk Loading

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

}

object TileFiller {
  final val SYMBOL = Symbol("filler")
  final val slotCount = 27

  trait Work {
    val working = true
  }

  object Wait extends Work {
    override val working = false
    override val toString = "Wait"
  }

}
