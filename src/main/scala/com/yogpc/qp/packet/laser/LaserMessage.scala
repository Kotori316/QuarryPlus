package com.yogpc.qp.packet.laser

import java.io.IOException
import java.util.Objects

import com.yogpc.qp.QuarryPlus
import com.yogpc.qp.packet.IMessage
import com.yogpc.qp.tile.TileLaser
import net.minecraft.client.Minecraft
import net.minecraft.network.PacketBuffer
import net.minecraft.util.math.{BlockPos, Vec3d}
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

object LaserMessage {
    def create(laser: TileLaser): LaserMessage = {
        val message = new LaserMessage(laser)
        message
    }
}

/**
  * To client only.
  */
class LaserMessage(laser: TileLaser) extends IMessage {
    private[this] var pos = BlockPos.ORIGIN
    private[this] var vec3ds: Array[Vec3d] = _

    def this() {
        this(null)
    }

    if (laser != null) {
        pos = laser.getPos
        vec3ds = laser.lasers
    }

    @throws[IOException]
    override def fromBytes(buffer: PacketBuffer): Unit = {
        pos = buffer.readBlockPos
        vec3ds = new Array[Vec3d](buffer.readInt)
        for (i <- vec3ds.indices) {
            vec3ds(i) = new Vec3d(buffer.readDouble, buffer.readDouble, buffer.readDouble)
        }
    }

    override def toBytes(buffer: PacketBuffer): Unit = {
        buffer.writeBlockPos(pos)
        buffer.writeInt(vec3ds.count(Objects.nonNull))
        for (vec3d <- vec3ds if vec3d != null) {
            buffer.writeDouble(vec3d.x).writeDouble(vec3d.y).writeDouble(vec3d.z)
        }
    }

    @SideOnly(Side.CLIENT) override def onRecieve(message: IMessage, ctx: MessageContext): IMessage = {
        val world = QuarryPlus.proxy.getPacketWorld(ctx.netHandler)
        val laser = world.getTileEntity(pos).asInstanceOf[TileLaser]
        if (laser != null) Minecraft.getMinecraft.addScheduledTask(new Runnable {
            override def run(): Unit = laser.lasers = vec3ds
        })
        null
    }
}
