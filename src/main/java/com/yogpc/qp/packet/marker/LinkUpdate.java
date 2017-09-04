package com.yogpc.qp.packet.marker;

import java.io.IOException;

import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.tile.TileMarker;
import net.minecraft.client.Minecraft;
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

    public static LinkUpdate create(TileMarker marker) {
        LinkUpdate linkUpdate = new LinkUpdate();
        linkUpdate.pos = marker.getPos();
        return linkUpdate;
    }

    @Override
    public void fromBytes(PacketBuffer buffer) throws IOException {
        pos = buffer.readBlockPos();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeBlockPos(pos);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IMessage onRecieve(IMessage message, MessageContext ctx) {
        TileMarker marker = (TileMarker) Minecraft.getMinecraft().world.getTileEntity(pos);
        assert marker != null;
        marker.G_updateSignal();
        return null;
    }
}
