package com.yogpc.qp.packet.quarry;

import java.util.Objects;
import java.util.function.Supplier;

import com.yogpc.qp.Config;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.quarry.TileQuarry;
import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.packet.PacketHandler;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;

import static jp.t2v.lab.syntax.MapStreamSyntax.optCast;

/**
 * To client only.
 */
public class MoveHead implements IMessage<MoveHead> {
    int dim;
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
        dim = buffer.readInt();
        headPosX = buffer.readDouble();
        headPosY = buffer.readDouble();
        headPosZ = buffer.readDouble();
        return this;
    }

    @Override
    public void writeToBuffer(PacketBuffer buffer) {
        buffer.writeBlockPos(pos).writeInt(dim);
        buffer.writeDouble(headPosX).writeDouble(headPosY).writeDouble(headPosZ);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void onReceive(Supplier<NetworkEvent.Context> ctx) {
        if (Config.client().enableRender().get()) {
            QuarryPlus.proxy.getPacketWorld(ctx.get())
                .filter(world -> world.getDimension().getType().getId() == dim)
                .filter(world -> world.isBlockLoaded(pos))
                .map(world -> world.getTileEntity(pos))
                .flatMap(optCast(TileQuarry.class))
                .ifPresent(quarry -> {
                    quarry.headPosX = headPosX;
                    quarry.headPosY = headPosY;
                    quarry.headPosZ = headPosZ;
                });
        }
    }
}
