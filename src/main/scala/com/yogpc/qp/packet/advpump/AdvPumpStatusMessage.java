package com.yogpc.qp.packet.advpump;

import java.util.function.Supplier;

import com.yogpc.qp.machines.advpump.TileAdvPump;
import com.yogpc.qp.packet.IMessage;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * To Client only.
 */
public class AdvPumpStatusMessage implements IMessage<AdvPumpStatusMessage> {

    ResourceLocation dim;
    BlockPos pos;
    boolean placeFrame;
    CompoundNBT nbtTagCompound;

    public static AdvPumpStatusMessage create(TileAdvPump pump) {
        AdvPumpStatusMessage message = new AdvPumpStatusMessage();
        message.dim = IMessage.getDimId(pump.getWorld());
        message.pos = pump.getPos();
        message.nbtTagCompound = pump.getUpdateTag();
        message.placeFrame = pump.placeFrame();
        return message;
    }

    @Override
    public AdvPumpStatusMessage readFromBuffer(PacketBuffer buffer) {
        pos = buffer.readBlockPos();
        nbtTagCompound = buffer.readCompoundTag();
        placeFrame = buffer.readBoolean();
        dim = buffer.readResourceLocation()();
        return this;
    }

    @Override
    public void writeToBuffer(PacketBuffer buffer) {
        buffer.writeBlockPos(pos).writeCompoundTag(nbtTagCompound).writeBoolean(placeFrame).writeInt(dim);
    }

    @Override
    public void onReceive(Supplier<NetworkEvent.Context> ctx) {
        IMessage.findTile(ctx, pos, dim, TileAdvPump.class)
            .ifPresent(pump -> ctx.get().enqueueWork(() -> pump.receiveStatusMessage(placeFrame, nbtTagCompound)));
    }
}
