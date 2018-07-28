package com.yogpc.qp.packet.marker;

import java.io.IOException;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.tile.TileMarker;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * To client only.
 */
public class LinkReply implements IMessage {
    BlockPos pos;
    BlockPos linkMin;
    BlockPos linkMax;

    public static LinkReply create(TileMarker marker) {
        LinkReply reply = new LinkReply();
        reply.pos = marker.getPos();
        reply.linkMax = marker.link.maxPos();
        reply.linkMin = marker.link.minPos();
        return reply;
    }

    @Override
    public void fromBytes(PacketBuffer buffer) {
        pos = buffer.readBlockPos();
        linkMin = buffer.readBlockPos();
        linkMax = buffer.readBlockPos();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeBlockPos(pos).writeBlockPos(linkMin).writeBlockPos(linkMax);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IMessage onRecieve(IMessage message, MessageContext ctx) {
        TileMarker marker = (TileMarker) QuarryPlus.proxy.getPacketWorld(ctx.netHandler).getTileEntity(pos);
        if (marker != null) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                if (marker.link != null) {
                    marker.link.removeConnection(false);
                }
                marker.link = new TileMarker.Link(marker.getWorld(), linkMax, linkMin);
                marker.link.init();
                marker.link.makeLaser();
                marker.G_updateSignal();
            });
        }
        return null;
    }
}
