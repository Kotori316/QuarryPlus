package com.yogpc.qp.packet.advquarry;

import java.io.IOException;
import java.util.Optional;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.compat.INBTWritable;
import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.tile.TileAdvQuarry;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * To Server Only
 */
public class AdvRangeMessage implements IMessage {
    int dim;
    BlockPos pos;
    NBTTagCompound rangeNBT;

    public static AdvRangeMessage create(TileAdvQuarry quarry) {
        AdvRangeMessage message = new AdvRangeMessage();
        message.dim = quarry.getWorld().provider.getDimension();
        message.pos = quarry.getPos();
        INBTWritable digRange = quarry.digRange();
        NBTTagCompound nbt = new NBTTagCompound();
        message.rangeNBT = digRange.writeToNBT(nbt);

        return message;
    }

    @Override
    public void fromBytes(PacketBuffer buffer) throws IOException {
        pos = buffer.readBlockPos();
        dim = buffer.readInt();
        rangeNBT = buffer.readNBTTagCompoundFromBuffer();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeBlockPos(pos).writeInt(dim);
        buffer.writeNBTTagCompoundToBuffer(rangeNBT);
    }

    @Override
    public IMessage onRecieve(IMessage message, MessageContext ctx) {
        World world = QuarryPlus.proxy.getPacketWorld(ctx.netHandler);
        if (world.provider.getDimension() == dim) {
            TileEntity entity = world.getTileEntity(pos);
            if (entity instanceof TileAdvQuarry) {
                TileAdvQuarry quarry = (TileAdvQuarry) entity;
                Optional.ofNullable(world.getMinecraftServer()).ifPresent(s -> s.addScheduledTask(() ->
                    quarry.digRange_$eq(TileAdvQuarry.DigRange$.MODULE$.readFromNBT(rangeNBT))));
            }
        }
        return null;
    }
}
