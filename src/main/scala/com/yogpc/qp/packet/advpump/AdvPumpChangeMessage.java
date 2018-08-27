package com.yogpc.qp.packet.advpump;

import java.util.Optional;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.tile.TileAdvPump;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * To Server only
 */
public class AdvPumpChangeMessage implements IMessage {
    int dim;
    BlockPos pos;
    boolean placeFrame;
    ToStart toStart;

    public static AdvPumpChangeMessage create(TileAdvPump tile, ToStart start) {
        AdvPumpChangeMessage message = new AdvPumpChangeMessage();
        message.placeFrame = tile.placeFrame();
        message.pos = tile.getPos();
        message.dim = tile.getWorld().provider.getDimension();
        message.toStart = start;
        return message;
    }

    @Override
    public void fromBytes(PacketBuffer buffer) {
        pos = buffer.readBlockPos();
        dim = buffer.readInt();
        placeFrame = buffer.readBoolean();
        toStart = ToStart.valueOf(buffer.readInt());
    }

    @Override
    public void toBytes(PacketBuffer buffer) {
        buffer.writeBlockPos(pos).writeInt(dim).writeBoolean(placeFrame).writeInt(toStart.ordinal());
    }

    @Override
    public IMessage onRecieve(IMessage message, MessageContext ctx) {
        World world = QuarryPlus.proxy.getPacketWorld(ctx.netHandler);
        MinecraftServer server = world.getMinecraftServer();
        if (world.provider.getDimension() == dim && server != null) {
            Optional.ofNullable((TileAdvPump) world.getTileEntity(pos)).ifPresent(tileAdvPump ->
                server.addScheduledTask(() -> {
                    tileAdvPump.placeFrame_$eq(placeFrame);
                    if (toStart == ToStart.START) {
                        tileAdvPump.start();
                    }
                }));
        }
        return null;
    }

    public enum ToStart {
        UNCHANGED, START, STOP;

        public static ToStart valueOf(int i) {
            switch (i) {
                case 0:
                    return UNCHANGED;
                case 1:
                    return START;
                case 2:
                    return STOP;
                default:
                    QuarryPlus.LOGGER.error("ToStart undefined enum = " + i);
                    return null;
            }
        }
    }
}
