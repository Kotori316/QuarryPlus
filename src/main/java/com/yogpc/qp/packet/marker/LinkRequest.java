package com.yogpc.qp.packet.marker;

import java.io.IOException;

import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.tile.TileMarker;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * To server only.
 */
public class LinkRequest implements IMessage {
    BlockPos pos;

    public static LinkRequest create(TileMarker marker) {
        LinkRequest request = new LinkRequest();
        request.pos = marker.getPos();
        return request;
    }

    @Override
    public void fromBytes(PacketBuffer buffer) throws IOException {
        pos = buffer.readBlockPos();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeBlockPos(pos);
    }

    //TODO not work...
    @Override
    public LinkReply onRecieve(IMessage message, MessageContext ctx) {
        TileMarker marker = (TileMarker) ctx.getServerHandler().playerEntity.world.getTileEntity(pos);
        if (marker != null) {
            if (marker.link != null)
                return LinkReply.create(marker);
        }
        return null;
    }
}
