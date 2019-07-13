package com.yogpc.qp.packet.quarry;

import java.util.function.Supplier;

import com.yogpc.qp.machines.quarry.TileQuarry2;
import com.yogpc.qp.packet.IMessage;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

public class Level2Message extends LevelMessage {
    @Override
    public Level2Message readFromBuffer(PacketBuffer buffer) {
        super.readFromBuffer(buffer);
        return this;
    }

    public static Level2Message create(TileQuarry2 quarry) {
        Level2Message message = new Level2Message();
        message.yLevel = quarry.yLevel();
        message.pos = quarry.getPos();
        message.dim = IMessage.getDimId(quarry.getWorld());
        return message;
    }

    @Override
    public void onReceive(Supplier<NetworkEvent.Context> ctx) {
        IMessage.findTile(ctx, pos, dim, TileQuarry2.class)
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
