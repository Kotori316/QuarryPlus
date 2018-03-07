package com.yogpc.qp.container

import com.yogpc.qp.packet.PacketHandler
import com.yogpc.qp.packet.enchantment.DiffMessage
import com.yogpc.qp.tile.TileBasic
import com.yogpc.qp.version.VersionUtil
import net.minecraft.entity.player.{EntityPlayer, EntityPlayerMP}
import net.minecraft.inventory.{Container, IContainerListener}
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

class ContainerEnchList(val tile: TileBasic, val player: EntityPlayer) extends Container {

    if (!tile.getWorld.isRemote)
        PacketHandler.sendToClient(DiffMessage.create(this, tile.fortuneList, tile.silktouchList), player.asInstanceOf[EntityPlayerMP])

    override def canInteractWith(ep: EntityPlayer): Boolean = ep.getDistanceSqToCenter(tile.getPos) <= 64.0D

    private var includeFlag = 0

    private def getInclude = ((if (this.tile.fortuneInclude) 2 else 0) | (if (this.tile.silktouchInclude) 1 else 0)).toByte

    override def detectAndSendChanges(): Unit = {
        super.detectAndSendChanges()
        val ninc = getInclude
        if (this.includeFlag != ninc) {
            this.includeFlag = ninc
            import scala.collection.JavaConverters._
            this.listeners.asScala.foreach(VersionUtil.sendWindowProperty(_, this, 0, includeFlag))
        }
    }

    override def addListener(listener: IContainerListener): Unit = {
        super.addListener(listener)
        VersionUtil.sendWindowProperty(listener, this, 0, getInclude)
    }

    @SideOnly(Side.CLIENT)
    override def updateProgressBar(i: Int, data: Int): Unit = if (i == 0) {
        this.tile.fortuneInclude = (data & 2) != 0
        this.tile.silktouchInclude = (data & 1) != 0
    }
}
