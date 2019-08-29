package com.yogpc.qp.packet.pump;

import java.util.function.Supplier;

import com.yogpc.qp.machines.pump.TilePump;
import com.yogpc.qp.packet.IMessage;
import javax.annotation.Nullable;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * To client only.
 */
public class Now implements IMessage<Now> {

    @Nullable
    Direction facing;
    boolean working;
    BlockPos pos;
    int dim;

    public static Now create(TilePump pump) {
        Now now = new Now();
        now.facing = pump.connectTo;
        now.working = pump.G_working();
        now.pos = pump.getPos();
        now.dim = IMessage.getDimId(pump.getWorld());
        return now;
    }

    @Override
    public Now readFromBuffer(PacketBuffer buffer) {
        pos = buffer.readBlockPos();
        dim = buffer.readInt();
        int i = buffer.readVarInt();
        if (i == -1) {
            facing = null;
        } else {
            facing = Direction.byIndex(i);
        }
        working = buffer.readBoolean();
        return this;
    }

    @Override
    public void writeToBuffer(PacketBuffer buffer) {
        buffer.writeBlockPos(pos).writeInt(dim);
        if (facing == null) {
            buffer.writeVarInt(-1);
        } else {
            buffer.writeVarInt(facing.ordinal());
        }
        buffer.writeBoolean(working);
    }

    @Override
    public void onReceive(Supplier<NetworkEvent.Context> ctx) {
        IMessage.findTile(ctx, pos, dim, TilePump.class)
            .ifPresent(pump -> ctx.get().enqueueWork(() -> {
                pump.setConnectTo(facing);
                pump.setWorking(working);
            }));
    }

}
