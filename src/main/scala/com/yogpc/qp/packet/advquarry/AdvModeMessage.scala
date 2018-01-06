package com.yogpc.qp.packet.advquarry

import com.yogpc.qp.QuarryPlus
import com.yogpc.qp.packet.IMessage
import com.yogpc.qp.tile.TileAdvQuarry
import net.minecraft.client.Minecraft
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.network.PacketBuffer
import net.minecraft.util.math.BlockPos
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

object AdvModeMessage {
    def create(quarry: TileAdvQuarry) = new AdvModeMessage(quarry)
}

/**
  * To Client Only
  */
class AdvModeMessage(quarry: TileAdvQuarry) extends IMessage {
    private[this] var dim = 0
    private[this] var pos = BlockPos.ORIGIN
    private[this] var modeNBT: NBTTagCompound = _
    if (quarry != null) {
        dim = quarry.getWorld.provider.getDimension
        pos = quarry.getPos
        val nbt = new NBTTagCompound
        modeNBT = quarry.digRange.writeToNBT(quarry.mode.writeToNBT(nbt))
    }

    def this() {
        this(null)
    }

    override def fromBytes(buffer: PacketBuffer) = {
        pos = buffer.readBlockPos
        dim = buffer.readInt
        modeNBT = buffer.readCompoundTag
    }

    override def toBytes(buffer: PacketBuffer) = {
        buffer.writeBlockPos(pos).writeInt(dim)
        buffer.writeCompoundTag(modeNBT)
    }

    @SideOnly(Side.CLIENT)
    override def onRecieve(message: IMessage, ctx: MessageContext) = {
        val world = QuarryPlus.proxy.getPacketWorld(ctx.netHandler)
        if (world.provider.getDimension == dim) world.getTileEntity(pos) match {
            case quarry: TileAdvQuarry =>
                Minecraft.getMinecraft.addScheduledTask(IMessage.toRunnable(() => quarry.recieveModeMassage(modeNBT)))
            case _ =>
        }
        null
    }
}
