package com.yogpc.qp.container

import com.yogpc.qp.packet.PacketHandler
import com.yogpc.qp.packet.advpump.AdvPumpStatusMessage
import com.yogpc.qp.tile.TileAdvPump
import com.yogpc.qp.version.VersionUtil
import net.minecraft.entity.player.{EntityPlayer, EntityPlayerMP}
import net.minecraft.inventory.{Container, Slot}
import net.minecraft.item.ItemStack

class ContainerAdvPump(tile: TileAdvPump, player: EntityPlayer) extends Container {

    val oneBox = 18

    for (h <- 0 until 3)
        for (v <- 0 until 9)
            this.addSlotToContainer(new Slot(player.inventory, v + h * 9 + 9, 8 + v * oneBox, 84 + h * oneBox))

    for (vertical <- 0 until 9)
        this.addSlotToContainer(new Slot(player.inventory, vertical, 8 + vertical * oneBox, 142))

    if (!tile.getWorld.isRemote) {
        PacketHandler.sendToClient(
            AdvPumpStatusMessage.create(tile), player.asInstanceOf[EntityPlayerMP])
    }

    override def transferStackInSlot(playerIn: EntityPlayer, index: Int): ItemStack = VersionUtil.empty()

    override def canInteractWith(playerIn: EntityPlayer) = tile.getWorld.getTileEntity(tile.getPos) eq tile
}
