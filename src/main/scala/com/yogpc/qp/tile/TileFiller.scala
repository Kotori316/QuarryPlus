package com.yogpc.qp.tile

import com.yogpc.qp._
import com.yogpc.qp.container.ContainerQuarryModule.HasModuleInventory
import com.yogpc.qp.gui.TranslationKeys
import com.yogpc.qp.modules.IModuleItem
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.inventory.{InventoryBasic, InventoryHelper, ItemStackHelper}
import net.minecraft.item.{ItemBlock, ItemStack}
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.math.{BlockPos, ChunkPos}
import net.minecraft.util.text.TextComponentTranslation
import net.minecraft.util.{EnumFacing, ITickable, NonNullList}
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
    with HasModuleInventory
    with IChunkLoadTile {
  val inventory = new TileFiller.InventoryFiller
  private[this] final val moduleInventory = new QuarryModuleInventory(new TextComponentTranslation(TranslationKeys.filler), 5, this, refreshModules _, TileFiller.modulePredicate)
  var work: FillerWorks = FillerWorks.Wait
  var modules: List[IModule] = Nil
  var lastArea: Option[(BlockPos, BlockPos)] = None

  // TileEntity Overrides

  override def update(): Unit = {
    super.update()
    if (!getWorld.isRemote && !machineDisabled) {
      modules.foreach(_.invoke(IModule.Tick(this)))
      val preWorking = isWorking
      this.work.tick(this)
      this.work = this.work.next(this)
      if (preWorking ^ isWorking) {
        if (preWorking) { // finished.
          finishWork()
        } else { // started.
          startWork()
        }
      }
    }
  }

  override def readFromNBT(nbt: NBTTagCompound): Unit = {
    super.readFromNBT(nbt)
    val itemList = NonNullList.withSize(TileFiller.slotCount, ItemStack.EMPTY)
    ItemStackHelper.loadAllItems(nbt.getCompoundTag("inventory"), itemList)
    itemList.asScala.zipWithIndex.foreach { case (stack, i) => inventory.setInventorySlotContents(i, stack) }
    moduleInv.deserializeNBT(nbt.getCompoundTag("modules"))
    work = FillerWorks.fromNBT(nbt.getCompoundTag("work"))
  }

  override def writeToNBT(nbt: NBTTagCompound): NBTTagCompound = {
    val itemList = Range(0, TileFiller.slotCount).map(i => (i, inventory.getStackInSlot(i))).foldLeft(NonNullList.withSize(TileFiller.slotCount, ItemStack.EMPTY)) {
      case (l, (i, s)) => l.set(i, s); l
    }
    val itemTag = new NBTTagCompound
    ItemStackHelper.saveAllItems(itemTag, itemList)
    nbt.setTag("inventory", itemTag)
    nbt.setTag("modules", moduleInv.serializeNBT())
    nbt.setTag("work", work.toNBT)
    super.writeToNBT(nbt)
  }

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
    configure(TileFiller.power * APowerTile.MJToMicroMJ, TileFiller.power * APowerTile.MJToMicroMJ)
  }

  // Implemented methods

  override protected def isWorking: Boolean = this.work.working

  override protected def getSymbol: Symbol = TileFiller.SYMBOL

  override def getDebugName: String = TranslationKeys.filler

  override def getDebugMessages = Seq(
    "FillerWorks: " + this.work,
    "Modules: " + modules.mkString(", ")
  ).map(toComponentString).asJava

  override def moduleInv = this.moduleInventory

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

  // Methods

  def refreshModules(inv: QuarryModuleInventory): Unit = {
    modules = inv.moduleItems().asScala.flatMap(e => e.getKey.apply(e.getValue, this).toList).toList
  }

  def getWorkingWorld = getWorld

  private def getArea: Option[(BlockPos, BlockPos)] = {
    EnumFacing.HORIZONTALS.map(f => getWorld.getTileEntity(getPos.offset(f)))
      .collectFirst { case m: IMarker if m.hasLink =>
        val a = m.min() -> m.max()
        m.removeFromWorldWithItem().asScala.foreach(i => InventoryHelper.spawnItemStack(getWorld, getPos.getX + 0.5, getPos.getY + 1, getPos.getZ + 0.5, i))
        a
      }
      .orElse(lastArea)
  }

  def startFillAll(): Unit = {
    getArea.foreach { case (min, max) => this.work = new FillerWorks.FillAll(min, max); lastArea = Option(min, max) }
  }

  def startFillBox(): Unit = {
    getArea.foreach { case (min, max) => this.work = new FillerWorks.FillBox(min, max); lastArea = Option(min, max) }
  }
}

object TileFiller {
  final val SYMBOL = Symbol("Filler")
  final val slotCount = 27
  final val power = 5000
  final val modulePredicate: java.util.function.Predicate[IModuleItem] = new java.util.function.Predicate[IModuleItem] {
    private[this] final lazy val set = Set(
      QuarryPlusI.fuelModuleCreative.getSymbol,
      QuarryPlusI.fuelModuleNormal.getSymbol
    )

    override def test(t: IModuleItem): Boolean = set contains t.getSymbol
  }

  final class InventoryFiller extends InventoryBasic(TranslationKeys.filler, false, TileFiller.slotCount) {
    @scala.annotation.tailrec
    def firstBlock(i: Int = 0): Option[(ItemStack, Block)] = {
      if (i >= 0 && i < getSizeInventory) {
        val stack = getStackInSlot(i)
        if (stack.isEmpty) {
          firstBlock(i + 1)
        } else {
          stack.getItem match {
            case iB: ItemBlock if iB.getBlock.getDefaultState.getMaterial != Material.AIR => Option(stack -> iB.getBlock)
            case _ => firstBlock(i + 1)
          }
        }
      } else {
        None
      }
    }
  }

}
