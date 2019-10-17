package com.yogpc.qp.machines.base

import java.util.Objects

import com.yogpc.qp.QuarryPlus
import com.yogpc.qp.utils.Holder
import net.minecraft.entity.player.{PlayerEntity, PlayerInventory}
import net.minecraft.inventory.container.{INamedContainerProvider, Slot}
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos

/**
 * Before opening this container, make sure you have synced information for display with Server. (Client side)
 */
class StatusContainer(id: Int, player: PlayerEntity, pos: BlockPos)
  extends net.minecraft.inventory.container.Container(Holder.statusContainerType, id) {

  val tile = Objects.requireNonNull(player.getEntityWorld.getTileEntity(pos))
  val oneBox = 18

  for (h <- Range(0, 3); v <- Range(0, 9)) {
    this.addSlot(new Slot(player.inventory, v + h * 9 + 9, 8 + v * oneBox, 84 + h * oneBox))
  }
  for (vertical <- Range(0, 9)) {
    this.addSlot(new Slot(player.inventory, vertical, 8 + vertical * oneBox, 142))
  }

  override def canInteractWith(playerIn: PlayerEntity): Boolean = true

  override def transferStackInSlot(playerIn: PlayerEntity, index: Int): ItemStack = ItemStack.EMPTY
}

object StatusContainer {
  final val GUI_ID = QuarryPlus.modID + ":gui_" + "status"

  trait StatusProvider {
    def getStatusStrings: Seq[String]
  }

  class ContainerProvider(pos: BlockPos) extends INamedContainerProvider {
    override def getDisplayName = Holder.itemStatusChecker.getName

    override def createMenu(id: Int, inv: PlayerInventory, player: PlayerEntity) = new StatusContainer(id, player, pos)
  }

}
