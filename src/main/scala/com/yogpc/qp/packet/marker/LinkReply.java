package com.yogpc.qp.packet.marker;

import java.io.IOException;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.tile.TileMarker;
import javax.annotation.Nullable;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
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
    @Nullable
    NBTTagCompound tagCompound;

    public static LinkReply create(BlockPos pos, @Nullable TileMarker.Link link) {
        LinkReply reply = new LinkReply();
        reply.pos = pos;
        reply.tagCompound = link != null ? link.toNbt() : null;
        return reply;
    }

    @Override
    public void fromBytes(PacketBuffer buffer) throws IOException {
        pos = buffer.readBlockPos();
        tagCompound = buffer.readCompoundTag();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeBlockPos(pos).writeCompoundTag(tagCompound);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IMessage onReceive(IMessage message, MessageContext ctx) {
        TileMarker marker = (TileMarker) QuarryPlus.proxy.getPacketWorld(ctx.netHandler).getTileEntity(pos);
        if (marker != null) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                if (tagCompound == null) {
                    marker.link = null;
                } else {
                    marker.link = new TileMarker.Link(tagCompound);
                    marker.link.makeLaser(true);
                }
                marker.G_updateSignal();
            });
        }
        return null;
    }
}
