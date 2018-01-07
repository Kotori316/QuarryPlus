package com.yogpc.qp.packet.enchantment

import java.io.IOException

import com.yogpc.qp.QuarryPlus
import com.yogpc.qp.container.ContainerMover
import com.yogpc.qp.packet.IMessage
import net.minecraft.network.PacketBuffer
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext

object MoverMessage {
    def create(pos: BlockPos, id: Int): Move = {
        val move = new Move(pos, id)
        move
    }

    def create(pos: BlockPos, id: Int, d: ContainerMover.D): Cursor = {
        val cursor = new Cursor(pos, id, d)
        cursor
    }

    /**
      * To server only.
      * For container player opening.
      */
    class Move(var pos: BlockPos, var id: Int) extends IMessage {

        def this() {
            this(null, 0)
        }

        @throws[IOException]
        override def fromBytes(buffer: PacketBuffer): Unit = {
            pos = buffer.readBlockPos
            id = buffer.readInt
        }

        override def toBytes(buffer: PacketBuffer): Unit = buffer.writeBlockPos(pos).writeInt(id)

        override def onRecieve(message: IMessage, ctx: MessageContext): IMessage = {
            val server = QuarryPlus.proxy.getPacketWorld(ctx.netHandler).getMinecraftServer
            Option(server).foreach(_.addScheduledTask(IMessage.toRunnable(() => {
                val container = QuarryPlus.proxy.getPacketPlayer(ctx.netHandler).openContainer
                if (container.windowId == id) container.asInstanceOf[ContainerMover].moveEnchant()
            })))
            null
        }
    }

    /**
      * To server only.
      * For container player opening.
      */
    class Cursor(var pos: BlockPos, var id: Int, var d: ContainerMover.D) extends IMessage {

        def this() {
            this(null, 0, null)
        }

        @throws[IOException]
        override def fromBytes(buffer: PacketBuffer): Unit = {
            d = buffer.readEnumValue(classOf[ContainerMover.D])
            pos = buffer.readBlockPos
            id = buffer.readInt
        }

        override def toBytes(buffer: PacketBuffer): Unit = buffer.writeEnumValue(d).writeBlockPos(pos).writeInt(id)

        override def onRecieve(message: IMessage, ctx: MessageContext): IMessage = {
            val server = QuarryPlus.proxy.getPacketWorld(ctx.netHandler).getMinecraftServer
            Option(server).foreach(_.addScheduledTask(IMessage.toRunnable(() => {
                val container = QuarryPlus.proxy.getPacketPlayer(ctx.netHandler).openContainer
                if (container.windowId == id) container.asInstanceOf[ContainerMover].setAvail(d)
            })))
            null
        }
    }

}
