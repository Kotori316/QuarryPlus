package com.yogpc.qp.packet.controller

import java.io.IOException
import java.util

import com.yogpc.qp.gui.GuiController
import com.yogpc.qp.packet.IMessage
import net.minecraft.client.Minecraft
import net.minecraft.network.PacketBuffer
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import net.minecraftforge.fml.common.registry.EntityEntry
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

import scala.collection.JavaConverters._

object AvailableEntities {
    def create(pos: BlockPos, dim: Int, list: util.List[EntityEntry]): AvailableEntities = {
        new AvailableEntities(pos, dim, list.asScala)
    }
}

/**
  * To client only.
  */
class AvailableEntities(var pos: BlockPos, var dim: Int, list: Seq[EntityEntry]) extends IMessage {
    private[this] var entities: Seq[ResourceLocation] = _
    if (list != null) entities = list.map(_.getRegistryName)

    def this() {
        this(null, 0, null)
    }

    @throws[IOException]
    override def fromBytes(buffer: PacketBuffer): Unit = {
        pos = buffer.readBlockPos
        dim = buffer.readInt
        val i = buffer.readInt
        entities = Range(0, i).map(_ => new ResourceLocation(buffer.readString(Short.MaxValue)))

    }

    override def toBytes(buffer: PacketBuffer): Unit = {
        buffer.writeBlockPos(pos)
        buffer.writeInt(dim)
        buffer.writeInt(entities.size)
        entities.foreach(resourceLocation => buffer.writeString(resourceLocation.toString))
    }

    @SideOnly(Side.CLIENT) override def onRecieve(message: IMessage, ctx: MessageContext): IMessage = {
        Minecraft.getMinecraft.addScheduledTask(new Runnable {
            override def run(): Unit = Minecraft.getMinecraft.displayGuiScreen(new GuiController(dim, pos.getX, pos.getY, pos.getZ, entities.asJava))
        })
        null
    }
}
