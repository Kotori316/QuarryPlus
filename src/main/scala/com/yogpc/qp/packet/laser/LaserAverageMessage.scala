package com.yogpc.qp.packet.laser

import java.io.IOException

import com.yogpc.qp.QuarryPlus
import com.yogpc.qp.packet.IMessage
import com.yogpc.qp.tile.TileLaser
import net.minecraft.client.Minecraft
import net.minecraft.network.PacketBuffer
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

object LaserAverageMessage {
    def create(laser: TileLaser): LaserAverageMessage = {
        val message = new LaserAverageMessage(laser)
        message
    }
}

/**
  * To client only.
  */
class LaserAverageMessage(laser: TileLaser) extends IMessage {
    private[this] var pos = BlockPos.ORIGIN
    private[this] var powerAverage = .0

    def this() {
        this(null)
    }

    if (laser != null) {
        pos = laser.getPos
        powerAverage = laser.pa
    }

    @throws[IOException]
    override def fromBytes(buffer: PacketBuffer): Unit = {
        pos = buffer.readBlockPos
        powerAverage = buffer.readDouble
    }

    override def toBytes(buffer: PacketBuffer): Unit = buffer.writeBlockPos(pos).writeDouble(powerAverage)

    @SideOnly(Side.CLIENT) override def onRecieve(message: IMessage, ctx: MessageContext): IMessage = {
        val world = QuarryPlus.proxy.getPacketWorld(ctx.netHandler)
        val laser = world.getTileEntity(pos).asInstanceOf[TileLaser]
        if (laser != null) Minecraft.getMinecraft.addScheduledTask(new Runnable {
            override def run(): Unit = laser.pa = powerAverage
        })
        null
    }
}
