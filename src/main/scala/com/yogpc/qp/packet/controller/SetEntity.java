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

    ResourceLocation dim;
    BlockPos pos;
    ResourceLocation location;

    public static SetEntity create(ResourceLocation dim, BlockPos pos, ResourceLocation location) {
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
        dim = buffer.readResourceLocation();
        return this;
    }

    @Override
    public void writeToBuffer(PacketBuffer buffer) {
        buffer.writeBlockPos(pos).writeResourceLocation(location).writeResourceLocation(dim);

    }

    @Override
    public void onReceive(Supplier<NetworkEvent.Context> ctx) {
        QuarryPlus.proxy.getPacketWorld(ctx.get())
            .filter(world -> world.getDimensionKey().getLocation().equals(dim))
            .filter(world -> world.isBlockPresent(pos))
            .ifPresent(world -> BlockController.setSpawnerEntity(world, pos, location));
    }
}
