package com.yogpc.qp.container

import com.yogpc.qp.packet.{IMessage, PacketHandler}
import com.yogpc.qp.tile.HasInv
import com.yogpc.qp.version.VersionUtil
import net.minecraft.entity.player.{EntityPlayer, EntityPlayerMP}
import net.minecraft.inventory.{Container, Slot}
import net.minecraft.tileentity.TileEntity

class ContainerQuarryLevel[T <: TileEntity with HasInv](tile: T, player: EntityPlayer)(implicit messageFunc: T => _ <: IMessage) extends Container {
  val oneBox = 18

  for (h <- 0 until 3; v <- 0 until 9) {
    this.addSlotToContainer(new Slot(player.inventory, v + h * 9 + 9, 8 + v * oneBox, 84 + h * oneBox))
  }

  for (vertical <- 0 until 9) {
    this.addSlotToContainer(new Slot(player.inventory, vertical, 8 + vertical * oneBox, 142))
  }

  if (!tile.getWorld.isRemote)
    PacketHandler.sendToClient(messageFunc(tile), player.asInstanceOf[EntityPlayerMP])

  override def canInteractWith(playerIn: EntityPlayer) = tile.getWorld.getTileEntity(tile.getPos) eq tile

  override def transferStackInSlot(playerIn: EntityPlayer, index: Int) = VersionUtil.empty
}
