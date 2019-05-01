package com.yogpc.qp.packet.quarry;

import java.util.function.Supplier;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.quarry.TileBasic;
import com.yogpc.qp.packet.IMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

import static jp.t2v.lab.syntax.MapStreamSyntax.optCast;

/**
 * To both client and server.
 * Don't make this grouped with other messages.
 * This is a sub class to use Y Setter.
 */
public class LevelMessage implements IMessage<LevelMessage> {
    protected int yLevel;
    protected BlockPos pos;
    protected int dim;

    public static LevelMessage create(TileBasic tileBasic) {
        LevelMessage message = new LevelMessage();
        message.yLevel = tileBasic.yLevel;
        message.pos = tileBasic.getPos();
        message.dim = IMessage.getDimId(tileBasic.getWorld());
        return message;
    }

    @Override
    public LevelMessage readFromBuffer(PacketBuffer buffer) {
        pos = buffer.readBlockPos();
        dim = buffer.readInt();
        yLevel = buffer.readInt();
        return this;
    }

    @Override
    public void writeToBuffer(PacketBuffer buffer) {
        buffer.writeBlockPos(pos).writeInt(dim);
        buffer.writeInt(yLevel);
    }

    @Override
    public void onReceive(Supplier<NetworkEvent.Context> ctx) {
        QuarryPlus.proxy.getPacketWorld(ctx.get())
            .filter(world -> world.getDimension().getType().getId() == dim)
            .filter(world -> world.isBlockLoaded(pos))
            .map(world -> world.getTileEntity(pos))
            .flatMap(optCast(TileBasic.class))
            .ifPresent(tile -> {
                switch (ctx.get().getDirection().getReceptionSide()) {
                    case CLIENT:
                        tile.setYLevel(yLevel);
                        break;
                    case SERVER:
                        ctx.get().enqueueWork(() -> tile.setYLevel(yLevel));
                        break;
                }
            });
    }

}
