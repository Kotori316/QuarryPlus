package com.yogpc.qp.packet.advpump;

import java.io.IOException;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.tile.TileAdvPump;
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
 * To Client only.
 */
public class AdvPumpStatusMessage implements IMessage {

    BlockPos pos;
    boolean placeFrame;
    NBTTagCompound enchNBT;

    public static AdvPumpStatusMessage create(BlockPos pos, boolean placeFrame, NBTTagCompound enchNBT) {
        AdvPumpStatusMessage message = new AdvPumpStatusMessage();
        message.pos = pos;
        message.enchNBT = enchNBT;
        message.placeFrame = placeFrame;
        return message;
    }

    @Override
    public void fromBytes(PacketBuffer buffer) throws IOException {
        pos = buffer.readBlockPos();
        enchNBT = buffer.readCompoundTag();
        placeFrame = buffer.readBoolean();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeBlockPos(pos).writeCompoundTag(enchNBT).writeBoolean(placeFrame);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IMessage onRecieve(IMessage message, MessageContext ctx) {
        World world = QuarryPlus.proxy.getPacketWorld(ctx.netHandler);
        TileEntity entity = world.getTileEntity(pos);
        if (entity != null) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                TileAdvPump pump = (TileAdvPump) entity;
                pump.recieveStatusMessage(placeFrame, enchNBT);
            });
        }
        return null;
    }
}
