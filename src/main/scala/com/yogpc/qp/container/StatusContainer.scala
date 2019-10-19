package com.yogpc.qp.container

import java.util.Objects

import com.yogpc.qp.QuarryPlus
import com.yogpc.qp.packet.{PacketHandler, TileMessage}
import net.minecraft.entity.player.{EntityPlayer, EntityPlayerMP}
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack
import net.minecraft.util.math.BlockPos

/**
  * Before opening this container, make sure you have synced information for display with Server. (Client side)
  */
class StatusContainer(id: Int, player: EntityPlayer, pos: BlockPos)
  extends net.minecraft.inventory.Container {

  // 175, 225
  val tile = Objects.requireNonNull(player.getEntityWorld.getTileEntity(pos))
  val oneBox = 18

  for (h <- Range(0, 3); v <- Range(0, 9)) {
    this.addSlotToContainer(new Slot(player.inventory, v + h * 9 + 9, 8 + v * oneBox, 144 + h * oneBox))
  }
  for (vertical <- Range(0, 9)) {
    this.addSlotToContainer(new Slot(player.inventory, vertical, 8 + vertical * oneBox, 202))
  }

  override def canInteractWith(playerIn: EntityPlayer): Boolean = true

  override def transferStackInSlot(playerIn: EntityPlayer, index: Int): ItemStack = ItemStack.EMPTY

  override def detectAndSendChanges(): Unit = {
    super.detectAndSendChanges()
    PacketHandler.sendToClient(TileMessage.create(tile), player.asInstanceOf[EntityPlayerMP])
  }
}

object StatusContainer {
  final val GUI_ID = QuarryPlus.modID + ":gui_" + "status"

  trait StatusProvider {
    def getStatusStrings: Seq[String]
  }

}
