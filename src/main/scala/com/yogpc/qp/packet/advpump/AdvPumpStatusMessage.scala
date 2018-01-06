package com.yogpc.qp.packet.advpump

import java.io.IOException

import com.yogpc.qp.QuarryPlus
import com.yogpc.qp.packet.IMessage
import com.yogpc.qp.tile.TileAdvPump
import net.minecraft.client.Minecraft
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.PacketBuffer
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

/**
  * To Client only.
  */
object AdvPumpStatusMessage {
    def create(pump: TileAdvPump): AdvPumpStatusMessage = new AdvPumpStatusMessage(pump)
}

class AdvPumpStatusMessage(pump: TileAdvPump) extends IMessage {
    private[this] var dim = 0
    private[this] var pos = BlockPos.ORIGIN
    private[this] var placeFrame = false
    private[this] var nbtTagCompound: NBTTagCompound = _

    def this() {
        this(null)
    }

    if (pump != null) {
        dim = pump.getWorld.provider.getDimension
        pos = pump.getPos
        nbtTagCompound = pump.getUpdateTag
        placeFrame = pump.placeFrame
    }

    @throws[IOException]
    override def fromBytes(buffer: PacketBuffer): Unit = {
        pos = buffer.readBlockPos
        nbtTagCompound = buffer.readCompoundTag
        placeFrame = buffer.readBoolean
        dim = buffer.readInt
    }

    override def toBytes(buffer: PacketBuffer): Unit = buffer.writeBlockPos(pos).writeCompoundTag(nbtTagCompound).writeBoolean(placeFrame).writeInt(dim)

    @SideOnly(Side.CLIENT)
    override def onRecieve(message: IMessage, ctx: MessageContext): IMessage = {
        val world = QuarryPlus.proxy.getPacketWorld(ctx.netHandler)
        if (world.provider.getDimension == dim) {
            world.getTileEntity(pos) match {
                case pump: TileAdvPump => Minecraft.getMinecraft.addScheduledTask(IMessage.toRunnable(() =>
                    pump.recieveStatusMessage(placeFrame, nbtTagCompound)
                ))
                case _ =>
            }
        }
        null
    }
}