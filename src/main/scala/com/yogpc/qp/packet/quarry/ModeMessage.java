package com.yogpc.qp.packet.quarry;

import java.util.function.Supplier;

import com.yogpc.qp.machines.quarry.TileQuarry;
import com.yogpc.qp.packet.IMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * To client only.
 */
public class ModeMessage implements IMessage<ModeMessage> {
    int dim;
    BlockPos pos;
    BlockPos minPos, maxPos;
    TileQuarry.Mode mode;

    public static ModeMessage create(TileQuarry quarry) {
        ModeMessage message = new ModeMessage();
        message.dim = IMessage.getDimId(quarry.getWorld());
        message.pos = quarry.getPos();
        message.mode = quarry.G_getNow();
        message.minPos = quarry.getMinPos();
        message.maxPos = quarry.getMaxPos();
        return message;
    }

    @Override
    public ModeMessage readFromBuffer(PacketBuffer buffer) {
        pos = buffer.readBlockPos();
        minPos = buffer.readBlockPos();
        maxPos = buffer.readBlockPos();
        mode = buffer.readEnumValue(TileQuarry.Mode.class);
        dim = buffer.readInt();
        return this;
    }

    @Override
    public void writeToBuffer(PacketBuffer buffer) {
        buffer.writeBlockPos(pos).writeBlockPos(minPos).writeBlockPos(maxPos).writeEnumValue(mode).writeInt(dim);

    }

    @Override
    public void onReceive(Supplier<NetworkEvent.Context> ctx) {
        IMessage.findTile(ctx, pos, dim, TileQuarry.class)
            .ifPresent(quarry -> ctx.get().enqueueWork(() -> {
                quarry.setNow(mode);
                quarry.xMin = minPos.getX();
                quarry.yMin = minPos.getY();
                quarry.zMin = minPos.getZ();
                quarry.xMax = maxPos.getX();
                quarry.yMax = maxPos.getY();
                quarry.zMax = maxPos.getZ();
//                    quarry.G_renew_powerConfigure();
//                IBlockState state = world.getBlockState(pos);
//                world.notifyBlockUpdate(pos, state, state, 3);
            }));
    }

}
