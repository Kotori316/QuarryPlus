package com.yogpc.qp.machines.item

import com.yogpc.qp.QuarryPlus
import com.yogpc.qp.machines.advquarry.TileAdvQuarry
import com.yogpc.qp.machines.quarry.{TileBasic, TileQuarry2}
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.inventory.Container
import net.minecraft.util.INameable
import net.minecraft.world.IInteractionObject

abstract class YSetterInteractionObject(nameable: INameable) extends IInteractionObject {

  override def getGuiID = YSetterInteractionObject.GUI_ID

  override def getName = nameable.getName

  override def hasCustomName = nameable.hasCustomName

  override def getCustomName = nameable.getCustomName
}

object YSetterInteractionObject {
  final val GUI_ID = QuarryPlus.modID + ":gui_" + QuarryPlus.Names.ySetter

  import GuiQuarryLevel._

  def apply(tile: INameable): IInteractionObject = tile match {
    case basic: TileBasic => new Basic(basic)
    case quarry: TileAdvQuarry => new AdvQuarry(quarry)
    case quarry2: TileQuarry2 => new Quarry2(quarry2)
  }

  private class Basic(basic: TileBasic) extends YSetterInteractionObject(basic) {
    override def createContainer(playerInventory: InventoryPlayer, playerIn: EntityPlayer) = new ContainerQuarryLevel(basic, playerIn)
  }

  private class AdvQuarry(quarry: TileAdvQuarry) extends YSetterInteractionObject(quarry) {
    override def createContainer(playerInventory: InventoryPlayer, playerIn: EntityPlayer): Container = new ContainerQuarryLevel(quarry, playerIn)
  }

  private class Quarry2(quarry: TileQuarry2) extends YSetterInteractionObject(quarry) {
    override def createContainer(playerInventory: InventoryPlayer, playerIn: EntityPlayer): Container = new ContainerQuarryLevel(quarry, playerIn)
  }

}