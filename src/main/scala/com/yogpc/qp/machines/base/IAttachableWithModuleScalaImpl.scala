package com.yogpc.qp.machines.base

import cats.implicits._
import com.yogpc.qp.machines.quarry.ContainerQuarryModule
import net.minecraft.util.Direction

import scala.jdk.CollectionConverters._

trait IAttachableWithModuleScalaImpl extends IAttachable {
  def self: APowerTile with ContainerQuarryModule.HasModuleInventory with HasStorage
  = this.asInstanceOf[APowerTile with ContainerQuarryModule.HasModuleInventory with HasStorage]

  var attachments: Map[IAttachment.Attachments[_], Direction] = Map.empty

  var modules: List[IModule] = Nil

  override def connectAttachment(facing: Direction, attachment: IAttachment.Attachments[_ <: APacketTile], simulate: Boolean): Boolean = {
    val tile = self.getWorld.getTileEntity(self.getPos.offset(facing))
    if (!attachments.get(attachment).exists(_ != facing) && attachment.test(tile)) {
      if (!simulate) {
        attachments = attachments.updated(attachment, facing)
        refreshModules()
      }
      true
    } else {
      false
    }
  }

  def refreshModules(): Unit = {
    val attachmentModules = attachments.toList >>= {
      case (kind, facing) => kind.module(self.getWorld.getTileEntity(self.getPos.offset(facing))).toList
    }
    val internalModules = self.moduleInv.moduleItems().asScala.toList >>= (e => e.getKey.apply(e.getValue, self).toList)
    this.modules = attachmentModules ++ internalModules
  }

  def neighborChanged(): Unit = {
    attachments = attachments.filter { case (kind, facing) => kind.test(self.getWorld.getTileEntity(self.getPos.offset(facing))) }
    refreshModules()
  }

}
