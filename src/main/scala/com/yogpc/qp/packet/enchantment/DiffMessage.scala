package com.yogpc.qp.packet.enchantment

import java.io.IOException
import java.util

import com.yogpc.qp.container.ContainerEnchList
import com.yogpc.qp.packet.IMessage
import com.yogpc.qp.{BlockData, QuarryPlus}
import net.minecraft.client.Minecraft
import net.minecraft.inventory.Container
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.PacketBuffer
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

import scala.collection.JavaConverters._

/**
  * To client only.
  * For container player opening.
  */
object DiffMessage {
    def create(container: Container, fortuneList: util.List[BlockData], silkList: util.List[BlockData]): DiffMessage = {
        new DiffMessage(container, fortuneList.asScala, silkList.asScala)
    }
}

class DiffMessage(var container: Container, var fortuneList: Seq[BlockData], var silkList: Seq[BlockData]) extends IMessage {
    private[this] var containerId = 0
    if (container != null) {
        containerId = container.windowId
    }

    def this() {
        this(null, null, null)
    }

    @throws[IOException]
    override def fromBytes(buffer: PacketBuffer): Unit = {
        containerId = buffer.readInt
        val fS = buffer.readInt
        val sS = buffer.readInt
        fortuneList = Range(0, fS).map(_ => BlockData.readFromNBT(buffer.readCompoundTag))
        silkList = Range(0, sS).map(_ => BlockData.readFromNBT(buffer.readCompoundTag))
    }

    override def toBytes(buffer: PacketBuffer): Unit = {
        buffer.writeInt(containerId)
        buffer.writeInt(fortuneList.size).writeInt(silkList.size)
        fortuneList.map(_.writeToNBT(new NBTTagCompound)).foreach(buffer.writeCompoundTag)
        silkList.map(_.writeToNBT(new NBTTagCompound)).foreach(buffer.writeCompoundTag)
    }

    @SideOnly(Side.CLIENT)
    override def onRecieve(message: IMessage, ctx: MessageContext): IMessage = {
        Minecraft.getMinecraft.addScheduledTask(IMessage.toRunnable(() => {
            val container = QuarryPlus.proxy.getPacketPlayer(ctx.netHandler).openContainer
            if (containerId == container.windowId && container.isInstanceOf[ContainerEnchList]) {
                val enchList = container.asInstanceOf[ContainerEnchList]
                enchList.tile.fortuneList.clear()
                fortuneList.foreach(enchList.tile.fortuneList.add)
                enchList.tile.silktouchList.clear()
                silkList.foreach(enchList.tile.silktouchList.add)
            }
        }))
        null
    }
}