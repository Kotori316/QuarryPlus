package com.yogpc.qp.packet.quarry2;

import java.util.function.Supplier;

import com.yogpc.qp.machines.quarry.QuarryAction;
import com.yogpc.qp.machines.quarry.TileQuarry2;
import com.yogpc.qp.packet.IMessage;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * To client only.
 */
public class ActionMessage implements IMessage<ActionMessage> {
    int dim;
    BlockPos pos;
    NBTTagCompound actionNBT;

    public static ActionMessage create(TileQuarry2 quarry2) {
        ActionMessage message = new ActionMessage();
        message.dim = IMessage.getDimId(quarry2.getWorld());
        message.pos = quarry2.getPos();
        message.actionNBT = quarry2.action().clientWrite(new NBTTagCompound());
        return message;
    }

    @Override
    public ActionMessage readFromBuffer(PacketBuffer buffer) {
        pos = buffer.readBlockPos();
        dim = buffer.readInt();
        actionNBT = buffer.readCompoundTag();
        return this;
    }

    @Override
    public void writeToBuffer(PacketBuffer buffer) {
        buffer.writeBlockPos(pos).writeInt(dim);
        buffer.writeCompoundTag(actionNBT);
    }

    @Override
    public void onReceive(Supplier<NetworkEvent.Context> ctx) {
        IMessage.findTile(ctx, pos, dim, TileQuarry2.class).ifPresent(quarry2 -> ctx.get().enqueueWork(() -> {
            QuarryAction action = QuarryAction.loadFromNBT().apply(actionNBT).apply(quarry2);
            quarry2.action_$eq(action);
        }));
    }
}
