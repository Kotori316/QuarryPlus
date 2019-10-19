package com.yogpc.qp.machines.base

import java.util.Objects

import com.yogpc.qp.QuarryPlus
import com.yogpc.qp.utils.Holder
import net.minecraft.entity.player.{PlayerEntity, PlayerInventory}
import net.minecraft.inventory.container.{INamedContainerProvider, Slot}
import net.minecraft.item.ItemStack
import net.minecraft.util.IntReferenceHolder
import net.minecraft.util.math.BlockPos
import net.minecraftforge.api.distmarker.{Dist, OnlyIn}

/**
 * Before opening this container, make sure you have synced information for display with Server. (Client side)
 */
class StatusContainer(id: Int, player: PlayerEntity, pos: BlockPos)
  extends net.minecraft.inventory.container.Container(Holder.statusContainerType, id) {

  // 175, 225
  val tile = Objects.requireNonNull(player.getEntityWorld.getTileEntity(pos))
  val oneBox = 18

  for (h <- Range(0, 3); v <- Range(0, 9)) {
    this.addSlot(new Slot(player.inventory, v + h * 9 + 9, 8 + v * oneBox, 144 + h * oneBox))
  }
  for (vertical <- Range(0, 9)) {
    this.addSlot(new Slot(player.inventory, vertical, 8 + vertical * oneBox, 202))
  }
  tile match {
    case provider: StatusContainer.StatusProvider => provider.tracks.foreach(this.trackInt)
    case _ =>
  }

  override def canInteractWith(playerIn: PlayerEntity): Boolean = true

  override def transferStackInSlot(playerIn: PlayerEntity, index: Int): ItemStack = ItemStack.EMPTY

  override def detectAndSendChanges(): Unit = {
    tile match {
      case provider: StatusContainer.StatusProvider =>
        provider.updateIntRef(provider.tracks)
      case _ =>
    }
    super.detectAndSendChanges()
  }
}

object StatusContainer {
  final val GUI_ID = QuarryPlus.modID + ":gui_" + "status"

  trait StatusProvider {
    @OnlyIn(Dist.CLIENT)
    def getStatusStrings(trackInts: Seq[IntReferenceHolder]): Seq[String]

    lazy val tracks: Seq[IntReferenceHolder] = Nil

    def updateIntRef(trackingInts: Seq[IntReferenceHolder]): Unit = ()
  }

  class ContainerProvider(pos: BlockPos) extends INamedContainerProvider {
    override def getDisplayName = Holder.itemStatusChecker.getName

    override def createMenu(id: Int, inv: PlayerInventory, player: PlayerEntity) = new StatusContainer(id, player, pos)
  }

}
