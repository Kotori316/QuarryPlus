package com.yogpc.qp.packet;

import java.io.IOException;
import java.util.Optional;

import com.yogpc.qp.QuarryPlus;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
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
        World world = QuarryPlus.proxy.getPacketWorld(ctx.netHandler);
        Runnable runnable = () -> Optional.ofNullable(world.getTileEntity(pos)).ifPresent(entity -> entity.readFromNBT(compound));
        FMLCommonHandler.instance().getWorldThread(ctx.netHandler).addScheduledTask(runnable);
        return null;
    }
}
