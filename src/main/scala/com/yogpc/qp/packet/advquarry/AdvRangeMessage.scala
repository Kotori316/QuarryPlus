package com.yogpc.qp.packet.advquarry

import java.io.IOException

import com.yogpc.qp.QuarryPlus
import com.yogpc.qp.packet.IMessage
import com.yogpc.qp.tile.TileAdvQuarry
import com.yogpc.qp.tile.TileAdvQuarry.DigRange
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.PacketBuffer
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext

object AdvRangeMessage {
    def create(quarry: TileAdvQuarry): AdvRangeMessage = new AdvRangeMessage(quarry)
}

/**
  * To Server Only
  */
class AdvRangeMessage(quarry: TileAdvQuarry) extends IMessage {
    private[this] var dim = 0
    private[this] var pos = BlockPos.ORIGIN
    private[this] var rangeNBT: NBTTagCompound = _

    if (quarry != null) {
        dim = quarry.getWorld.provider.getDimension
        pos = quarry.getPos
        val digRange = quarry.digRange
        val nbt = new NBTTagCompound
        rangeNBT = digRange.writeToNBT(nbt)
    }

    def this() {
        this(null)
    }

    @throws[IOException]
    override def fromBytes(buffer: PacketBuffer): Unit = {
        pos = buffer.readBlockPos
        dim = buffer.readInt
        rangeNBT = buffer.readCompoundTag
    }

    override def toBytes(buffer: PacketBuffer): Unit = {
        buffer.writeBlockPos(pos).writeInt(dim)
        buffer.writeCompoundTag(rangeNBT)
    }

    override def onRecieve(message: IMessage, ctx: MessageContext): IMessage = {
        val world = QuarryPlus.proxy.getPacketWorld(ctx.netHandler)
        if (world.provider.getDimension == dim) {
            world.getTileEntity(pos) match {
                case quarry: TileAdvQuarry =>
                    Option(world.getMinecraftServer).foreach(s => {
                        s.addScheduledTask(IMessage.toRunnable(() => {
                            quarry.digRange = DigRange.readFromNBT(rangeNBT)
                        }))
                    })
                case _ =>
            }
        }
        null
    }
}
