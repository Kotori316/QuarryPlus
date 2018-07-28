package com.yogpc.qp.packet.pump;

import java.io.IOException;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.tile.TilePump;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * To client only.
 */
public class Now implements IMessage {

    EnumFacing facing;
    boolean working;
    BlockPos blockPos;

    public static Now create(TilePump pump) {
        Now now = new Now();
        now.facing = pump.connectTo;
        now.working = pump.G_working();
        now.blockPos = pump.getPos();
        return now;
    }

    @Override
    public void fromBytes(PacketBuffer buffer) {
        int i = buffer.readVarInt();
        if (i == -1) {
            facing = null;
        } else {
            facing = EnumFacing.getFront(i);
        }
        working = buffer.readBoolean();
        blockPos = buffer.readBlockPos();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        if (facing == null) {
            buffer.writeVarInt(-1);
        } else {
            buffer.writeVarInt(facing.ordinal());
        }
        buffer.writeBoolean(working);
        buffer.writeBlockPos(blockPos);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IMessage onRecieve(IMessage message, MessageContext ctx) {
        TileEntity entity = QuarryPlus.proxy.getPacketWorld(ctx.netHandler).getTileEntity(blockPos);
        if (TilePump.class.isInstance(entity)) {
            TilePump pump = (TilePump) entity;
            Minecraft.getMinecraft().addScheduledTask(() -> {
                pump.setConnectTo(facing);
                pump.setWorking(working);
            });
        }
        return null;
    }
}
