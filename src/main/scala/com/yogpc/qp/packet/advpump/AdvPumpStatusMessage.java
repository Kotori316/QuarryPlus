package com.yogpc.qp.packet.advpump;

import java.io.IOException;
import java.util.Optional;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.tile.TileAdvPump;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * To Client only.
 */
public class AdvPumpStatusMessage implements IMessage {

    int dim;
    BlockPos pos;
    boolean placeFrame;
    NBTTagCompound nbtTagCompound;

    public static AdvPumpStatusMessage create(TileAdvPump pump) {
        AdvPumpStatusMessage message = new AdvPumpStatusMessage();
        message.dim = pump.getWorld().provider.getDimension();
        message.pos = pump.getPos();
        message.nbtTagCompound = pump.getUpdateTag();
        message.placeFrame = pump.placeFrame();
        return message;
    }

    @Override
    public void fromBytes(PacketBuffer buffer) throws IOException {
        pos = buffer.readBlockPos();
        nbtTagCompound = buffer.readNBTTagCompoundFromBuffer();
        placeFrame = buffer.readBoolean();
        dim = buffer.readInt();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeBlockPos(pos).writeNBTTagCompoundToBuffer(nbtTagCompound).writeBoolean(placeFrame).writeInt(dim);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IMessage onRecieve(IMessage message, MessageContext ctx) {
        World world = QuarryPlus.proxy.getPacketWorld(ctx.netHandler);
        if (world.provider.getDimension() == dim) {
            Optional.ofNullable(world.getTileEntity(pos)).map(TileAdvPump.class::cast).ifPresent(pump ->
                Minecraft.getMinecraft().addScheduledTask(pump.recieveStatusMessage(placeFrame, nbtTagCompound)));
        }
        return null;
    }
}
