package com.yogpc.qp.packet.marker;

import java.io.IOException;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.tile.TileMarker;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * To Client only.
 */
public class LinkUpdate implements IMessage {
    BlockPos pos;
    boolean b;

    public static LinkUpdate create(TileMarker marker, boolean create) {
        LinkUpdate linkUpdate = new LinkUpdate();
        linkUpdate.pos = marker.getPos();
        linkUpdate.b = create;
        return linkUpdate;
    }

    @Override
    public void fromBytes(PacketBuffer buffer) throws IOException {
        pos = buffer.readBlockPos();
        b = buffer.readBoolean();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeBlockPos(pos).writeBoolean(b);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IMessage onRecieve(IMessage message, MessageContext ctx) {
        TileMarker marker = (TileMarker) QuarryPlus.proxy.getPacketWorld(ctx.netHandler).getTileEntity(pos);
        assert marker != null;
        if (b) {
            marker.laser = new TileMarker.Laser(marker.getWorld(), marker.getPos(), marker.link);
        } else {
            marker.G_updateSignal();
        }
        return null;
    }
}
