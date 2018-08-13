package com.yogpc.qp.packet.advquarry;

import java.io.IOException;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.compat.INBTWritable;
import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.tile.TileAdvQuarry;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * To Client Only
 */
public class AdvModeMessage implements IMessage {
    int dim;
    BlockPos pos;
    NBTTagCompound modeNBT;

    public static AdvModeMessage create(TileAdvQuarry quarry) {
        AdvModeMessage message = new AdvModeMessage();
        message.dim = quarry.getWorld().provider.getDimension();
        message.pos = quarry.getPos();
        INBTWritable mode = quarry.mode();
        INBTWritable digRange = quarry.digRange();
        NBTTagCompound nbt = new NBTTagCompound();
        message.modeNBT = digRange.writeToNBT(mode.writeToNBT(nbt));

        return message;
    }

    @Override
    public void fromBytes(PacketBuffer buffer) throws IOException {
        pos = buffer.readBlockPos();
        dim = buffer.readInt();
        modeNBT = buffer.readCompoundTag();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeBlockPos(pos).writeInt(dim);
        buffer.writeCompoundTag(modeNBT);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IMessage onRecieve(IMessage message, MessageContext ctx) {
        World world = QuarryPlus.proxy.getPacketWorld(ctx.netHandler);
        if (world.provider.getDimension() == dim) {
            TileEntity entity = world.getTileEntity(pos);
            if (entity instanceof TileAdvQuarry) {
                TileAdvQuarry quarry = (TileAdvQuarry) entity;
                Minecraft.getMinecraft().addScheduledTask(quarry.recieveModeMessage(modeNBT));
            }
        }
        return null;
    }
}
