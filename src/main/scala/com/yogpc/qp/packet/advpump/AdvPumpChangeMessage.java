package com.yogpc.qp.packet.advpump;

import java.util.function.Supplier;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.TranslationKeys;
import com.yogpc.qp.machines.advpump.TileAdvPump;
import com.yogpc.qp.machines.quarry.ContainerQuarryModule;
import com.yogpc.qp.packet.IMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * To Server only
 */
public class AdvPumpChangeMessage implements IMessage<AdvPumpChangeMessage> {
    int dim;
    BlockPos pos;
    boolean placeFrame;
    boolean deleteFluid;
    ToStart toStart;

    public static AdvPumpChangeMessage create(TileAdvPump tile, ToStart start) {
        AdvPumpChangeMessage message = new AdvPumpChangeMessage();
        message.placeFrame = tile.placeFrame();
        message.pos = tile.getPos();
        message.dim = IMessage.getDimId(tile.getWorld());
        message.toStart = start;
        message.deleteFluid = tile.delete();
        return message;
    }

    @Override
    public AdvPumpChangeMessage readFromBuffer(PacketBuffer buffer) {
        pos = buffer.readBlockPos();
        dim = buffer.readInt();
        placeFrame = buffer.readBoolean();
        toStart = ToStart.valueOf(buffer.readInt());
        deleteFluid = buffer.readBoolean();
        return this;
    }

    @Override
    public void writeToBuffer(PacketBuffer buffer) {
        buffer.writeBlockPos(pos).writeInt(dim).writeBoolean(placeFrame).writeInt(toStart.ordinal()).writeBoolean(deleteFluid);
    }

    @Override
    public void onReceive(Supplier<NetworkEvent.Context> ctx) {
        IMessage.findTile(ctx, pos, dim, TileAdvPump.class)
            .ifPresent(pump -> ctx.get().enqueueWork(() -> {
                if (toStart == ToStart.MODULE_INV) {
                    ContainerQuarryModule.InteractionObject.openGUI(pump, ctx.get().getSender(), TranslationKeys.advpump);
                } else {
                    pump.placeFrame_$eq(placeFrame);
                    pump.delete_$eq(deleteFluid);
                    if (toStart == ToStart.START) {
                        pump.start();
                    }
                }

            }));
    }

    public enum ToStart {
        UNCHANGED, START, STOP, MODULE_INV;

        public static ToStart valueOf(int i) {
            switch (i) {
                case 0:
                    return UNCHANGED;
                case 1:
                    return START;
                case 2:
                    return STOP;
                case 3:
                    return MODULE_INV;
                default:
                    QuarryPlus.LOGGER.error("ToStart undefined enum = " + i);
                    return null;
            }
        }
    }
}
