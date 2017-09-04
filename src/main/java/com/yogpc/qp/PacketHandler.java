/*
 * Copyright (C) 2012,2013 yogpstop This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.yogpc.qp;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.yogpc.qp.tile.APacketTile;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.FMLEmbeddedChannel;
import net.minecraftforge.fml.common.network.FMLOutboundHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;


@Sharable
public class PacketHandler extends SimpleChannelInboundHandler<YogpstopPacket> {
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Handler {
    }

    private static final Map<String, Method> registeredStaticHandlers = new HashMap<>();

    public static void registerStaticHandler(final Class<?> c) {
        final List<Method> l = ReflectionHelper.getMethods(c, Handler.class);
        if (l.size() == 1)
            registeredStaticHandlers.put(c.getName(), l.get(0));
    }

    public static EnumMap<Side, FMLEmbeddedChannel> channels;
    static final byte Tile = 0;
    static final byte NBT = 1;
    static final byte STATIC = 3;
    static final byte KEY = 4;

    public static final byte StC_NOW = 4;


    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final YogpstopPacket packet)
            throws Exception {
        if (packet.getChannel() == NBT)
            setNBTFromPacket(packet);
        else if (packet.getChannel() == Tile) {
            final ByteArrayDataInput hdr = ByteStreams.newDataInput(packet.getHeader());
            final TileEntity t = packet.getPlayer().world.getTileEntity(new BlockPos(hdr.readInt(), hdr.readInt(), hdr.readInt()));
            if (t instanceof APacketTile) {
                final APacketTile tb = (APacketTile) t;
//                if (tb.getWorld().isRemote)
//                    tb.C_recievePacket(hdr.readByte(), packet.getData(), packet.getPlayer());
//                else
//                    tb.S_recievePacket(hdr.readByte(), packet.getData(), packet.getPlayer());
            }
        } else if (packet.getChannel() == STATIC) {
            final ByteArrayDataInput hdr = ByteStreams.newDataInput(packet.getHeader());
            ReflectionHelper.invoke(registeredStaticHandlers.get(hdr.readUTF()), null, (Object) packet.getData());
        } else if (packet.getChannel() == KEY)
            QuarryPlus.proxy.setKeys(packet.getPlayer(), packet.getData()[0] << 24
                    | packet.getData()[1] << 16 | packet.getData()[2] << 8 | packet.getData()[3]);
    }

    public static void sendPacketToServer(final YogpstopPacket p) {
        channels.get(Side.CLIENT).attr(FMLOutboundHandler.FML_MESSAGETARGET)
                .set(FMLOutboundHandler.OutboundTarget.TOSERVER);
        channels.get(Side.CLIENT).writeOutbound(p);
    }

    public static void sendPacketToAround(final YogpstopPacket p, final int d, final int x,
                                          final int y, final int z) {
        channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET)
                .set(FMLOutboundHandler.OutboundTarget.ALLAROUNDPOINT);
        channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS)
                .set(new NetworkRegistry.TargetPoint(d, x, y, z, 256));
        channels.get(Side.SERVER).writeOutbound(p);
    }

    private static void setNBTFromPacket(final YogpstopPacket p) {
        try {
            final NBTTagCompound cache = CompressedStreamTools.read(ByteStreams.newDataInput(p.getData()), NBTSizeTracker.INFINITE);
            final TileEntity te = p.getPlayer().world.getTileEntity(new BlockPos(cache.getInteger("x"), cache.getInteger("y"), cache.getInteger("z")));
            if (te != null)
                te.readFromNBT(cache);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void sendNowPacket(final APacketTile te, final byte data) {
        sendPacketToAround(new YogpstopPacket(new byte[]{data}, te, StC_NOW),
                te.getWorld().provider.getDimension(), te.getPos().getX(), te.getPos().getY(), te.getPos().getZ());
    }

}
