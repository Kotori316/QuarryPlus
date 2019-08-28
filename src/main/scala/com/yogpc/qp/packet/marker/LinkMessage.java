package com.yogpc.qp.packet.marker;

import java.util.Optional;
import java.util.function.Supplier;

import com.yogpc.qp.machines.marker.TileMarker;
import com.yogpc.qp.packet.IMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.LogicalSidedProvider;
import net.minecraftforge.fml.network.NetworkEvent;

import static jp.t2v.lab.syntax.MapStreamSyntax.streamCast;

/**
 * To client only.
 */
public class LinkMessage implements IMessage<LinkMessage> {
    private BlockPos pos;
    private int dim;
    private BlockPos maxPos, minPos;

    public static LinkMessage create(TileMarker marker) {
        LinkMessage message = new LinkMessage();
        message.pos = marker.getPos();
        message.dim = IMessage.getDimId(marker.getWorld());
        message.maxPos = marker.max();
        message.minPos = marker.min();
        return message;
    }

    @Override
    public LinkMessage readFromBuffer(PacketBuffer buffer) {
        pos = buffer.readBlockPos();
        dim = buffer.readInt();
        minPos = buffer.readBlockPos();
        maxPos = buffer.readBlockPos();
        return this;
    }

    @Override
    public void writeToBuffer(PacketBuffer buffer) {
        buffer.writeBlockPos(pos).writeInt(dim);
        buffer.writeBlockPos(minPos).writeBlockPos(maxPos);
    }

    @Override
    public void onReceive(Supplier<NetworkEvent.Context> ctx) {
        TileMarker.Link link = TileMarker.Link.of(maxPos.getX(), minPos.getX(), maxPos.getY(), minPos.getY(), maxPos.getZ(), minPos.getZ());
        Optional<World> worldOptional = LogicalSidedProvider.CLIENTWORLD.get(ctx.get().getDirection().getReceptionSide());
        worldOptional.filter(w -> w.dimension.getType().getId() == dim).ifPresent(world -> {
            TileMarker.Link boxed = link.setWorld(world);
            ctx.get().enqueueWork(() ->
                boxed.edges()
                    .filter(p -> world.isAreaLoaded(p, 1))
                    .map(world::getTileEntity)
                    .flatMap(streamCast(TileMarker.class))
                    .forEach(marker -> marker.setLink(boxed)));
        });
    }
}
