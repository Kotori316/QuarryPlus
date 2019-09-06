package com.yogpc.qp.packet.advquarry;

import java.util.function.Supplier;

import com.yogpc.qp.machines.advquarry.AdvQuarryWork;
import com.yogpc.qp.machines.advquarry.TileAdvQuarry;
import com.yogpc.qp.machines.base.Area;
import com.yogpc.qp.packet.IMessage;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * To Client Only
 */
public class AdvModeMessage implements IMessage<AdvModeMessage> {
    int dim;
    BlockPos pos;
    CompoundNBT modeNBT;

    public static AdvModeMessage create(TileAdvQuarry quarry) {
        AdvModeMessage message = new AdvModeMessage();
        message.dim = IMessage.getDimId(quarry.getWorld());
        message.pos = quarry.getPos();
        AdvQuarryWork mode = quarry.action();
        Area digRange = quarry.area();
        CompoundNBT nbt = new CompoundNBT();
        CompoundNBT areaNbt = Area.areaToNbt().apply(digRange);
        nbt.put("area", areaNbt);
        CompoundNBT modeNbt = mode.serverWrite(new CompoundNBT());
        nbt.put("mode", modeNbt);
        message.modeNBT = nbt;

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
                quarry.area_$eq(Area.areaLoad(modeNBT.getCompound("area")));
                quarry.action_$eq(AdvQuarryWork.load().apply(quarry).apply(modeNBT.getCompound("mode")));
            }));
    }

}
