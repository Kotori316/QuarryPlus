package com.yogpc.qp.packet.mini_quarry;

import java.util.function.Supplier;

import com.yogpc.qp.machines.mini_quarry.MiniQuarryTile;
import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.packet.PacketHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * To Server Only.
 */
public class MiniRequestListMessage implements IMessage<MiniRequestListMessage> {
    private BlockPos pos;
    private int dim;

    public static MiniRequestListMessage create(MiniQuarryTile tile) {
        MiniRequestListMessage message = new MiniRequestListMessage();
        message.dim = IMessage.getDimId(tile.getWorld());
        message.pos = tile.getPos();
        return message;
    }

    @Override
    public MiniRequestListMessage readFromBuffer(PacketBuffer buffer) {
        MiniRequestListMessage message = new MiniRequestListMessage();
        message.pos = buffer.readBlockPos();
        message.dim = buffer.readInt();
        return message;
    }

    @Override
    public void writeToBuffer(PacketBuffer buffer) {
        buffer.writeBlockPos(pos).writeInt(dim);
    }

    @Override
    public void onReceive(Supplier<NetworkEvent.Context> ctx) {
        IMessage.findTile(ctx, pos, dim, MiniQuarryTile.class).ifPresent(t -> ctx.get().enqueueWork(() -> {
            PacketHandler.sendToClient(
                MiniListSyncMessage.create(pos, dim, t.blackList(), t.whiteList()), t.getWorld()
            );
        }));
    }
}
