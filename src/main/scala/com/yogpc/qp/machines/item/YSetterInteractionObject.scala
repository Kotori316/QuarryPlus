package com.yogpc.qp.machines.item

import com.yogpc.qp.QuarryPlus
import com.yogpc.qp.machines.quarry.{TileBasic, TileQuarry2}
import net.minecraft.entity.player.{PlayerEntity, PlayerInventory}
import net.minecraft.inventory.container.INamedContainerProvider
import net.minecraft.util.INameable
import net.minecraft.util.math.BlockPos

abstract class YSetterInteractionObject(nameable: INameable) extends INamedContainerProvider {

  def getGuiID = YSetterInteractionObject.GUI_ID

  override def getDisplayName = nameable.getName

}

object YSetterInteractionObject {
  final val GUI_ID = QuarryPlus.modID + ":gui_" + QuarryPlus.Names.ySetter

  def apply(tile: INameable, pos: BlockPos): YSetterInteractionObject = tile match {
    case basic: TileBasic => new Basic(basic, pos)
    /*case quarry: TileAdvQuarry => new AdvQuarry(quarry, pos)*/
    case quarry2: TileQuarry2 => new Quarry2(quarry2, pos)
  }

  private class Basic(basic: TileBasic, pos: BlockPos) extends YSetterInteractionObject(basic) {
    override def createMenu(id: Int, p_createMenu_2_ : PlayerInventory, playerIn: PlayerEntity) = new ContainerQuarryLevel(id, playerIn, pos)
  }

  /*private class AdvQuarry(quarry: TileAdvQuarry, pos: BlockPos) extends YSetterInteractionObject(quarry) {
    override def createMenu(id: Int, p_createMenu_2_ : PlayerInventory, playerIn: PlayerEntity) = new ContainerQuarryLevel(id, playerIn, pos)
  }*/

  private class Quarry2(quarry: TileQuarry2, pos: BlockPos) extends YSetterInteractionObject(quarry) {
    override def createMenu(id: Int, p_createMenu_2_ : PlayerInventory, playerIn: PlayerEntity) = new ContainerQuarryLevel(id, playerIn, pos)
  }

}