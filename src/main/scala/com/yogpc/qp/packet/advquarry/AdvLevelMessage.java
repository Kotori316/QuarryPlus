package com.yogpc.qp.packet.advquarry;

import java.util.function.Supplier;

import com.yogpc.qp.machines.advquarry.TileAdvQuarry;
import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.packet.quarry.LevelMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * To both client and server.
 * Don't make this grouped with other AdvMessages.
 * This is a sub class to use Y Setter.
 */
public class AdvLevelMessage extends LevelMessage {
    @Override
    public AdvLevelMessage readFromBuffer(PacketBuffer buffer) {
        super.readFromBuffer(buffer);
        return this;
    }

    public static AdvLevelMessage create(TileAdvQuarry quarry) {
        AdvLevelMessage message = new AdvLevelMessage();
        message.yLevel = quarry.yLevel();
        message.pos = quarry.getPos();
        message.dim = IMessage.getDimId(quarry.getWorld());
        return message;
    }

    @Override
    public void onReceive(Supplier<NetworkEvent.Context> ctx) {
        IMessage.findTile(ctx, pos, dim, TileAdvQuarry.class)
            .ifPresent(quarry -> {
                switch (ctx.get().getDirection().getReceptionSide()) {
                    case CLIENT:
                        quarry.yLevel_$eq(yLevel);
                        break;
                    case SERVER:
                        ctx.get().enqueueWork(() -> quarry.yLevel_$eq(yLevel));
                        break;
                }
            });
    }
}
