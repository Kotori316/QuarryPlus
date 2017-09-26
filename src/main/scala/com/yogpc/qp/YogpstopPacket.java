package com.yogpc.qp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.NetworkRegistry;

public final class YogpstopPacket {
    private byte[] header;
    private byte[] data;
    private EntityPlayer ep;

    public YogpstopPacket() {
    }

    public YogpstopPacket(final int c) {
        this.data = new byte[]{(byte) (c >>> 24), (byte) (c >>> 16), (byte) (c >>> 8), (byte) c};
        this.header = new byte[]{PacketHandler.KEY};
    }

    public EntityPlayer getPlayer() {
        return this.ep;
    }

    public byte getChannel() {
        return this.header[0];
    }

    public byte[] getData() {
        return this.data;
    }

    public void readData(final ByteBuf d, final ChannelHandlerContext ctx) {
        this.ep = QuarryPlus.proxy.getPacketPlayer(ctx.channel().attr(NetworkRegistry.NET_HANDLER).get());
        this.header = new byte[d.readInt()];
        d.readBytes(this.header);
        this.data = new byte[d.readableBytes()];
        d.readBytes(this.data);
    }

    public void writeData(final ByteBuf d) {
        d.writeInt(this.header.length);
        d.writeBytes(this.header);
        d.writeBytes(this.data);
    }
}
