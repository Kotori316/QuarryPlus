package com.yogpc.qp.machines.item

import com.yogpc.qp.machines.item.GuiQuarryLevel._
import com.yogpc.qp.machines.quarry.{TileBasic, TileQuarry2}
import com.yogpc.qp.packet.PacketHandler
import com.yogpc.qp.packet.quarry.LevelMessage
import com.yogpc.qp.utils.Holder
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.inventory.container.{Container, Slot}
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util.math.BlockPos

class ContainerQuarryLevel(id: Int, player: PlayerEntity, pos: BlockPos)
  extends Container(Holder.ySetterContainerType, id) {
  type Message[T <: TileEntity] = T => _ <: LevelMessage
  val tile = player.getEntityWorld.getTileEntity(pos)
  val oneBox = 18
  val messageFunc: Message[TileEntity] = (tile match {
    case _: TileBasic => implicitly[Message[TileBasic]]
    case _: TileQuarry2 => implicitly[Message[TileQuarry2]]
//    case _: TileAdvQuarry => implicitly[Message[TileAdvQuarry]]
    case _ => null
  }).asInstanceOf[Message[TileEntity]]

  for (h <- 0 until 3; v <- 0 until 9) {
    this.addSlot(new Slot(player.inventory, v + h * 9 + 9, 8 + v * oneBox, 84 + h * oneBox))
  }

  for (vertical <- 0 until 9) {
    this.addSlot(new Slot(player.inventory, vertical, 8 + vertical * oneBox, 142))
  }

  if (!tile.getWorld.isRemote)
    PacketHandler.sendToClient(messageFunc(tile), tile.getWorld)

  override def canInteractWith(playerIn: PlayerEntity) = tile.getWorld.getTileEntity(tile.getPos) eq tile

  override def transferStackInSlot(playerIn: PlayerEntity, index: Int) = ItemStack.EMPTY
}
