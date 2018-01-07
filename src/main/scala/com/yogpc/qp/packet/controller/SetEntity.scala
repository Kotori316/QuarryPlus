package com.yogpc.qp.packet.controller

import java.io.IOException

import com.yogpc.qp.QuarryPlus
import com.yogpc.qp.block.BlockController
import com.yogpc.qp.packet.IMessage
import net.minecraft.network.PacketBuffer
import net.minecraft.util.ResourceLocation
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext

object SetEntity {
    def create(dim: Int, pos: BlockPos, location: String): SetEntity = {
        val setEntity = new SetEntity(pos, dim, location)
        setEntity
    }
}

/**
  * To server only.
  */
class SetEntity(var pos: BlockPos, var dim: Int, var location: String) extends IMessage {

    def this() {
        this(null, 0, null)
    }

    @throws[IOException]
    override def fromBytes(buffer: PacketBuffer): Unit = {
        pos = buffer.readBlockPos
        location = buffer.readString(Short.MaxValue)
        dim = buffer.readInt
    }

    override def toBytes(buffer: PacketBuffer): Unit = buffer.writeBlockPos(pos).writeString(location).writeInt(dim)

    override def onRecieve(message: IMessage, ctx: MessageContext): IMessage = {
        val world = QuarryPlus.proxy.getPacketWorld(ctx.netHandler)
        if (world.provider.getDimension == dim)
            Option(world.getMinecraftServer).foreach(_.addScheduledTask(new Runnable {
                override def run(): Unit = BlockController.setSpawnerEntity(world, pos, new ResourceLocation(location))
            }))
        null
    }
}
