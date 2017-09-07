package com.yogpc.qp.packet.marker;

import java.io.IOException;

import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.tile.TileMarker;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * To server only.
 */
public class LinkRequest implements IMessage {
    BlockPos pos;
    int dim;

    public static LinkRequest create(TileMarker marker) {
        LinkRequest request = new LinkRequest();
        request.pos = marker.getPos();
        request.dim = marker.getWorld().provider.getDimension();
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
        World world = ctx.getServerHandler().playerEntity.world;
        if (world.provider.getDimension() == dim) {
            TileMarker marker = (TileMarker) world.getTileEntity(pos);
            if (marker != null) {
                if (marker.link != null)
                    return LinkReply.create(marker);
            }
        }
        return null;
    }
}
