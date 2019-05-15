package com.yogpc.qp.packet.controller;

import java.util.function.Supplier;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.controller.BlockController;
import com.yogpc.qp.packet.IMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * To server only.
 */
public class SetEntity implements IMessage<SetEntity> {

    int dim;
    BlockPos pos;
    ResourceLocation location;

    public static SetEntity create(int dim, BlockPos pos, ResourceLocation location) {
        SetEntity setEntity = new SetEntity();
        setEntity.dim = dim;
        setEntity.location = location;
        setEntity.pos = pos;
        return setEntity;
    }

    @Override
    public SetEntity readFromBuffer(PacketBuffer buffer) {
        pos = buffer.readBlockPos();
        location = buffer.readResourceLocation();
        dim = buffer.readInt();
        return this;
    }

    @Override
    public void writeToBuffer(PacketBuffer buffer) {
        buffer.writeBlockPos(pos).writeResourceLocation(location).writeInt(dim);

    }

    @Override
    public void onReceive(Supplier<NetworkEvent.Context> ctx) {
        QuarryPlus.proxy.getPacketWorld(ctx.get())
            .filter(world -> world.getDimension().getType().getId() == dim)
            .filter(world -> world.isBlockLoaded(pos))
            .ifPresent(world -> BlockController.setSpawnerEntity(world, pos, location));
    }
}
