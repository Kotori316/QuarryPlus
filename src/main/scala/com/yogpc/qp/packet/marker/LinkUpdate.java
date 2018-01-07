package com.yogpc.qp.packet.marker;

import java.io.IOException;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.tile.TileMarker;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * To Client only.
 */
public class LinkUpdate implements IMessage {
    BlockPos pos;
    int dim;
    boolean b;

    public static LinkUpdate create(TileMarker marker, boolean create) {
        LinkUpdate linkUpdate = new LinkUpdate();
        linkUpdate.pos = marker.getPos();
        linkUpdate.b = create;
        linkUpdate.dim = marker.getWorld().provider.getDimension();
        return linkUpdate;
    }

    @Override
    public void fromBytes(PacketBuffer buffer) throws IOException {
        pos = buffer.readBlockPos();
        b = buffer.readBoolean();
        dim = buffer.readInt();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeBlockPos(pos).writeBoolean(b).writeInt(dim);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IMessage onRecieve(IMessage message, MessageContext ctx) {
        World world = QuarryPlus.proxy.getPacketWorld(ctx.netHandler);
        if (world.provider.getDimension() == dim) {
            TileMarker marker = (TileMarker) world.getTileEntity(pos);
            if (marker != null) {
                Minecraft.getMinecraft().addScheduledTask(() -> {
                    if (b) {
                        marker.laser = new TileMarker.Laser(marker.getWorld(), marker.getPos(), marker.link);
                    } else {
                        marker.G_updateSignal();
                    }
                });
            }
        }
        return null;
    }
}
