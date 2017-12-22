package com.yogpc.qp.packet;

import java.io.IOException;

import com.yogpc.qp.QuarryPlus;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * To both client and server.
 */
public class TileMessage implements IMessage {
    NBTTagCompound compound;

    public static TileMessage create(TileEntity entity) {
        TileMessage message = new TileMessage();
        message.compound = entity.writeToNBT(new NBTTagCompound());
        return message;
    }

    @Override
    public void fromBytes(PacketBuffer buffer) throws IOException {
        compound = buffer.readCompoundTag();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeCompoundTag(compound);
    }

    @Override
    public IMessage onRecieve(IMessage message, MessageContext ctx) {
        BlockPos pos = new BlockPos(compound.getInteger("x"), compound.getInteger("y"), compound.getInteger("z"));
        TileEntity tileEntity = QuarryPlus.proxy.getPacketWorld(ctx.netHandler).getTileEntity(pos);
        if (tileEntity != null) {
            tileEntity.readFromNBT(compound);
        }
        return null;
    }
}
