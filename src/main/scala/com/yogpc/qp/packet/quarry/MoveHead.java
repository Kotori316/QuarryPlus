package com.yogpc.qp.packet.quarry;

import java.util.Objects;
import java.util.function.Supplier;

import com.yogpc.qp.Config;
import com.yogpc.qp.machines.quarry.TileQuarry;
import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.packet.PacketHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * To client only.
 */
public class MoveHead implements IMessage<MoveHead> {
    ResourceLocation dim;
    BlockPos pos;
    double headPosX;
    double headPosY;
    double headPosZ;

    public static MoveHead create(TileQuarry quarry) {
        MoveHead message = new MoveHead();
        message.dim = IMessage.getDimId(quarry.getWorld());
        message.pos = quarry.getPos();
        message.headPosX = quarry.headPosX;
        message.headPosY = quarry.headPosY;
        message.headPosZ = quarry.headPosZ;
        return message;
    }

    public static void send(TileQuarry quarry) {
        PacketHandler.sendToAround(create(quarry), Objects.requireNonNull(quarry.getWorld()), quarry.getPos());
    }

    @Override
    public MoveHead readFromBuffer(PacketBuffer buffer) {
        pos = buffer.readBlockPos();
        dim = buffer.readResourceLocation();
        headPosX = buffer.readDouble();
        headPosY = buffer.readDouble();
        headPosZ = buffer.readDouble();
        return this;
    }

    @Override
    public void writeToBuffer(PacketBuffer buffer) {
        buffer.writeBlockPos(pos).writeResourceLocation(dim);
        buffer.writeDouble(headPosX).writeDouble(headPosY).writeDouble(headPosZ);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onReceive(Supplier<NetworkEvent.Context> ctx) {
        if (Config.client().enableRender().get()) {
            IMessage.findTile(ctx, pos, dim, TileQuarry.class)
                .ifPresent(quarry -> {
                    quarry.headPosX = headPosX;
                    quarry.headPosY = headPosY;
                    quarry.headPosZ = headPosZ;
                });
        }
    }
}
