package com.yogpc.qp.machines.filler

import java.util
import java.util.Collections

import com.yogpc.qp.machines.TranslationKeys
import com.yogpc.qp.machines.base._
import com.yogpc.qp.machines.quarry.ContainerQuarryModule.HasModuleInventory
import com.yogpc.qp.utils.Holder
import net.minecraft.tileentity.ITickableTileEntity
import net.minecraft.util.text.ITextComponent

class FillerTile extends APowerTile(Holder.fillerType)
  with ITickableTileEntity
  with IDebugSender
  with HasModuleInventory
  with IChunkLoadTile
  with HasStorage.HasDummyStorage {

  private[this] final val moduleInventory = new QuarryModuleInventory(5, this, inv => updateModule(inv), _ => true)
  var modules: List[IModule] = List.empty

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

  def updateModule(i: QuarryModuleInventory): Unit = {
    import scala.jdk.CollectionConverters._
    modules = i.moduleItems().asScala
      .flatMap(e => e.getKey.apply(e.getValue, this))
      .toList
  }
}

object FillerTile {
  val SYMBOL: Symbol = Symbol("Filler")
}