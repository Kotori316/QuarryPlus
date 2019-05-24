package com.yogpc.qp.packet.advquarry;

import java.util.function.Supplier;

import com.yogpc.qp.machines.advquarry.TileAdvQuarry;
import com.yogpc.qp.packet.IMessage;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * To Client Only
 */
public class AdvModeMessage implements IMessage<AdvModeMessage> {
    int dim;
    BlockPos pos;
    NBTTagCompound modeNBT;

    public static AdvModeMessage create(TileAdvQuarry quarry) {
        AdvModeMessage message = new AdvModeMessage();
        message.dim = IMessage.getDimId(quarry.getWorld());
        message.pos = quarry.getPos();
        TileAdvQuarry.Mode mode = quarry.mode();
        TileAdvQuarry.DigRange digRange = quarry.digRange();
        NBTTagCompound nbt = new NBTTagCompound();
        message.modeNBT = digRange.writeToNBT(mode.writeToNBT(nbt));

        return message;
    }

    @Override
    public AdvModeMessage readFromBuffer(PacketBuffer buffer) {
        pos = buffer.readBlockPos();
        dim = buffer.readInt();
        modeNBT = buffer.readCompoundTag();
        return this;
    }

    @Override
    public void writeToBuffer(PacketBuffer buffer) {
        buffer.writeBlockPos(pos).writeInt(dim);
        buffer.writeCompoundTag(modeNBT);
    }

    @Override
    public void onReceive(Supplier<NetworkEvent.Context> ctx) {
        IMessage.findTile(ctx, pos, dim, TileAdvQuarry.class)
            .ifPresent(quarry -> ctx.get().enqueueWork(() -> {
                quarry.mode().readFromNBT(modeNBT);
                quarry.digRange_$eq(TileAdvQuarry.DigRange$.MODULE$.readFromNBT(modeNBT));
            }));
    }

}
