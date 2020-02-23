package com.yogpc.qp.tile

import com.yogpc.qp._
import com.yogpc.qp.container.ContainerQuarryModule.HasModuleInventory
import com.yogpc.qp.gui.TranslationKeys
import com.yogpc.qp.modules.IModuleItem
import net.minecraft.inventory.{InventoryBasic, ItemStackHelper}
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.math.ChunkPos
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
  val inventory = new InventoryBasic(TranslationKeys.filler, false, TileFiller.slotCount)
  private[this] final val moduleInventory = new QuarryModuleInventory(new TextComponentTranslation(TranslationKeys.filler), 5, this, refreshModules _, TileFiller.modulePredicate)
  var work: TileFiller.Work = TileFiller.Wait
  var modules: List[IModule] = Nil

  // TileEntity Overrides

  override def update(): Unit = {
    super.update()
    if (!getWorld.isRemote && !machineDisabled) {
      modules.foreach(_.invoke(IModule.Tick(this)))
      this.work.tick(this)
      this.work = this.work.next(this)
    }
  }

  override def readFromNBT(nbt: NBTTagCompound): Unit = {
    super.readFromNBT(nbt)
    val itemList = NonNullList.withSize(TileFiller.slotCount, ItemStack.EMPTY)
    ItemStackHelper.loadAllItems(nbt.getCompoundTag("inventory"), itemList)
    itemList.asScala.zipWithIndex.foreach { case (stack, i) => inventory.setInventorySlotContents(i, stack) }
    moduleInv.deserializeNBT(nbt.getCompoundTag("modules"))
    work = TileFiller.Work.fromNBT(nbt.getCompoundTag("work"))
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
    configure(5000 * APowerTile.MJToMicroMJ, 5000 * APowerTile.MJToMicroMJ)
  }

  // Implemented methods

  override protected def isWorking: Boolean = this.work.working

  override protected def getSymbol: Symbol = TileFiller.SYMBOL

  override def getDebugName: String = TranslationKeys.filler

  override def getDebugMessages = Seq(
    "Work: " + this.work,
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
}

object TileFiller {
  final val SYMBOL = Symbol("Filler")
  final val slotCount = 27
  final val modulePredicate: java.util.function.Predicate[IModuleItem] = new java.util.function.Predicate[IModuleItem] {
    private[this] final lazy val set = Set(
      QuarryPlusI.fuelModuleCreative.getSymbol,
      QuarryPlusI.fuelModuleNormal.getSymbol
    )

    override def test(t: IModuleItem): Boolean = set contains t.getSymbol
  }

  trait Work {
    val name: String
    val working = true

    override def toString = name

    def tick(tile: TileFiller): Unit

    def next(tile: TileFiller): Work

    final def toNBT: NBTTagCompound = {
      val tag = new NBTTagCompound
      tag.setString("name", this.name)
      save(tag)
    }

    protected def save(tag: NBTTagCompound): NBTTagCompound
  }

  object Work {
    def fromNBT(tag: NBTTagCompound): Work = {
      val name = tag.getString("name")
      restoreMap.get(name).map(_.apply(tag)).getOrElse {
        QuarryPlus.LOGGER.error(s"Unregistered key $name was used to read work $tag.")
        Wait
      }
    }

    private[this] final val restoreMap: Map[String, NBTTagCompound => Work] = Map(
      Wait.name -> (_ => Wait)
    )
  }

  object Wait extends Work {
    override val working = false
    override val name = "Wait"

    override def tick(tile: TileFiller): Unit = ()

    override def next(tile: TileFiller): Work = this

    protected override def save(tag: NBTTagCompound): NBTTagCompound = tag
  }

}
