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

import java.util.EnumMap;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.minecraftforge.fml.common.network.FMLEmbeddedChannel;
import net.minecraftforge.fml.common.network.FMLOutboundHandler;
import net.minecraftforge.fml.relauncher.Side;

@Sharable
public class PacketHandler extends SimpleChannelInboundHandler<YogpstopPacket> {

    public static EnumMap<Side, FMLEmbeddedChannel> channels;
    static final byte KEY = 4;


    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final YogpstopPacket packet)
            throws Exception {
        if (packet.getChannel() == KEY)
            QuarryPlus.proxy.setKeys(packet.getPlayer(), packet.getData()[0] << 24
                    | packet.getData()[1] << 16 | packet.getData()[2] << 8 | packet.getData()[3]);
    }

    public static void sendPacketToServer(final YogpstopPacket p) {
        channels.get(Side.CLIENT).attr(FMLOutboundHandler.FML_MESSAGETARGET)
                .set(FMLOutboundHandler.OutboundTarget.TOSERVER);
        channels.get(Side.CLIENT).writeOutbound(p);
    }

}
