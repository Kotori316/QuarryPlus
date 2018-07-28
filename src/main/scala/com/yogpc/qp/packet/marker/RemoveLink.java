package com.yogpc.qp.packet.marker;

import java.io.IOException;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.tile.TileMarker;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * To client only.
 */
public class RemoveLink implements IMessage {

    BlockPos minPos, maxPos;
    int dimensionId;

    public static RemoveLink create(BlockPos minPos, BlockPos maxPos, int dimensionId) {
        RemoveLink link = new RemoveLink();
        link.dimensionId = dimensionId;
        link.maxPos = maxPos;
        link.minPos = minPos;
        return link;
    }

    @Override
    public void fromBytes(PacketBuffer buffer) {
        minPos = buffer.readBlockPos();
        maxPos = buffer.readBlockPos();
        dimensionId = buffer.readInt();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeBlockPos(minPos).writeBlockPos(maxPos).writeInt(dimensionId);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IMessage onRecieve(IMessage message, MessageContext ctx) {
        World world = QuarryPlus.proxy.getPacketWorld(ctx.netHandler);
        if (world.provider.getDimension() == dimensionId) {
            final int index = TileMarker.linkList.indexOf(new TileMarker.Link(world, maxPos, minPos));
            if (index >= 0) {
                TileMarker.linkList.get(index).removeConnection(false);
            }
        }
        return null;
    }
}
