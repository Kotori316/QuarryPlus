package com.yogpc.qp.packet.distiller

import java.io.IOException

import com.yogpc.qp.QuarryPlus
import com.yogpc.qp.packet.IMessage
import com.yogpc.qp.tile.TileRefinery
import net.minecraft.client.Minecraft
import net.minecraft.network.PacketBuffer
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

object AnimatonMessage {
    def create(refinery: TileRefinery): AnimatonMessage = new AnimatonMessage(refinery)
}

/**
  * To Client only.
  */
class AnimatonMessage(refinery: TileRefinery) extends IMessage {
    var dim = 0
    var pos = BlockPos.ORIGIN
    var speed = .0f
    var stage = 0
    if (refinery != null) {
        pos = refinery.getPos
        dim = refinery.getWorld.provider.getDimension
        speed = refinery.animationSpeed
        stage = refinery.getAnimationStage
    }

    def this() {
        this(null)
    }

    @throws[IOException]
    override def fromBytes(buffer: PacketBuffer): Unit = {
        pos = buffer.readBlockPos
        dim = buffer.readInt
        speed = buffer.readFloat
        stage = buffer.readInt
    }

    override def toBytes(buffer: PacketBuffer): Unit = buffer.writeBlockPos(pos).writeInt(dim).writeFloat(speed).writeInt(stage)

    @SideOnly(Side.CLIENT)
    override def onRecieve(message: IMessage, ctx: MessageContext): IMessage = {
        val world = QuarryPlus.proxy.getPacketWorld(ctx.netHandler)
        if (world.provider.getDimension == dim) {
            world.getTileEntity(pos) match {
                case refinery: TileRefinery => Minecraft.getMinecraft.addScheduledTask(refinery.receiveMessage(stage, speed))
                case _ =>
            }
        }
        null
    }
}
