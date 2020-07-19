package com.yogpc.qp.machines.filler

import java.util
import java.util.Collections

import com.yogpc.qp.machines.TranslationKeys
import com.yogpc.qp.machines.base._
import com.yogpc.qp.machines.quarry.ContainerQuarryModule.HasModuleInventory
import com.yogpc.qp.utils.Holder
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.entity.player.{PlayerEntity, PlayerInventory}
import net.minecraft.inventory.Inventory
import net.minecraft.inventory.container.{Container, INamedContainerProvider}
import net.minecraft.item.{BlockItem, ItemStack}
import net.minecraft.nbt.{CompoundNBT, ListNBT}
import net.minecraft.tileentity.ITickableTileEntity
import net.minecraft.util.Direction
import net.minecraft.util.text.ITextComponent
import net.minecraftforge.common.capabilities.Capability
import net.minecraftforge.common.util.Constants.NBT
import net.minecraftforge.common.util.LazyOptional
import net.minecraftforge.items.CapabilityItemHandler
import net.minecraftforge.items.wrapper.InvWrapper

import scala.util.chaining._

class FillerTile extends APowerTile(Holder.fillerType)
  with ITickableTileEntity
  with IDebugSender
  with HasModuleInventory
  with IChunkLoadTile
  with HasStorage.HasDummyStorage
  with INamedContainerProvider {

  private[this] final val moduleInventory = new QuarryModuleInventory(5, this, inv => updateModule(inv), _ => true)
  var modules: List[IModule] = List.empty
  val inventory = new FillerTile.InventoryFiller

  override protected def workInTick(): Unit = ()

  override protected def isWorking: Boolean = false

  override def getDebugName: String = TranslationKeys.filler

  override def getDebugMessages: util.List[_ <: ITextComponent] = Collections.emptyList()

  override def moduleInv: QuarryModuleInventory = moduleInventory

  override def getModules: List[IModule] = modules

  override def remove(): Unit = {
    super[IChunkLoadTile].releaseTicket()
    super.remove()
  }

  override def read(nbt: CompoundNBT): Unit = {
    super.read(nbt)
    moduleInventory.deserializeNBT(nbt.getCompound("moduleInventory"))
    inventory.read(nbt.getList("inventory", NBT.TAG_COMPOUND))
  }

  override def write(nbt: CompoundNBT): CompoundNBT = {
    nbt.put("moduleInventory", moduleInventory.serializeNBT())
    nbt.put("inventory", inventory.write())
    super.write(nbt)
  }

  override def getCapability[T](cap: Capability[T], side: Direction): LazyOptional[T] = {
    if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
      CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.orEmpty(cap, LazyOptional.of(() => new InvWrapper(inventory)))
    } else
      super.getCapability(cap, side)
  }

  def updateModule(i: QuarryModuleInventory): Unit = {
    import scala.jdk.CollectionConverters._
    modules = i.moduleItems().asScala
      .flatMap(e => e.getKey.apply(e.getValue, this))
      .toList
  }

  override def createMenu(id: Int, i: PlayerInventory, p: PlayerEntity): Container = new FillerContainer(id, p, getPos)
}

object FillerTile {
  val SYMBOL: Symbol = Symbol("Filler")
  final val slotCount = 27
  final val power = 5000

  final class InventoryFiller extends Inventory(FillerTile.slotCount) {
    @scala.annotation.tailrec
    def firstBlock(i: Int = 0): Option[(ItemStack, Block)] = {
      if (i >= 0 && i < getSizeInventory) {
        val stack = getStackInSlot(i)
        if (stack.isEmpty) {
          firstBlock(i + 1)
        } else {
          stack.getItem match {
            case iB: BlockItem if iB.getBlock.getDefaultState.getMaterial != Material.AIR => Option(stack -> iB.getBlock)
            case _ => firstBlock(i + 1)
          }
        }
      } else {
        None
      }
    }

    override def read(list: ListNBT): Unit = {
      import scala.jdk.CollectionConverters._
      list.asScala.collect { case t: CompoundNBT => (t.getShort("Slot"), ItemStack.read(t)) }
        .foreach(e => setInventorySlotContents(e._1, e._2))
    }

    override def write: ListNBT = {
      val nbt = for {
        i <- 0 until getSizeInventory
        stack = getStackInSlot(i)
      } yield {
        stack.serializeNBT().tap(_.putShort("Slot", i.toShort))
      }
      val list = new ListNBT()
      nbt.foreach(list.add)
      list
    }
  }

}
