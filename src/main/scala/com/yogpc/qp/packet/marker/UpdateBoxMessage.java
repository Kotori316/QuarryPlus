package com.yogpc.qp.packet.marker;

import java.util.function.Supplier;

import com.yogpc.qp.machines.marker.TileMarker;
import com.yogpc.qp.packet.IMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * To client only.
 */
public class UpdateBoxMessage implements IMessage<UpdateBoxMessage> {
    private BlockPos pos;
    private int dim;
    private boolean on;

    public static UpdateBoxMessage create(TileMarker marker, boolean on) {
        UpdateBoxMessage message = new UpdateBoxMessage();
        message.dim = IMessage.getDimId(marker.getWorld());
        message.pos = marker.getPos();
        message.on = on;
        return message;
    }

    @Override
    public UpdateBoxMessage readFromBuffer(PacketBuffer buffer) {
        pos = buffer.readBlockPos();
        dim = buffer.readInt();
        on = buffer.readBoolean();
        return this;
    }

    @Override
    public void writeToBuffer(PacketBuffer buffer) {
        buffer.writeBlockPos(pos).writeInt(dim);
        buffer.writeBoolean(on);
    }

    @Override
    public void onReceive(Supplier<NetworkEvent.Context> ctx) {
        IMessage.findTile(ctx, pos, dim, TileMarker.class)
            .ifPresent(marker -> marker.laser.boxUpdate(marker.getWorld(), on));
    }
}
