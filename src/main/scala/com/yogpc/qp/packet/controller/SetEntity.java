package com.yogpc.qp.packet.controller;

import java.io.IOException;
import java.util.Optional;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.block.BlockController;
import com.yogpc.qp.packet.IMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * To server only.
 */
public class SetEntity implements IMessage {

    int dim;
    BlockPos pos;
    String location;

    public static SetEntity create(int dim, BlockPos pos, String location) {
        SetEntity setEntity = new SetEntity();
        setEntity.dim = dim;
        setEntity.location = location;
        setEntity.pos = pos;
        return setEntity;
    }

    @Override
    public void fromBytes(PacketBuffer buffer) throws IOException {
        pos = buffer.readBlockPos();
        location = buffer.readString(Short.MAX_VALUE);
        dim = buffer.readInt();
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeBlockPos(pos).writeString(location).writeInt(dim);
    }

    @Override
    public IMessage onRecieve(IMessage message, MessageContext ctx) {
        World world = QuarryPlus.proxy.getPacketWorld(ctx.netHandler);
        if (world.provider.getDimension() == dim) {
            Optional.ofNullable(world.getMinecraftServer()).ifPresent(s -> s.addScheduledTask(() ->
                    BlockController.setSpawnerEntity(world, pos, new ResourceLocation(location))));
        }
        return null;
    }
}
