package com.yogpc.qp.machines.mini_quarry;

import com.yogpc.qp.Holder;
import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.packet.PacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

/**
 * To Server only.
 */
public final class MiniRequestListMessage implements IMessage {
    private final BlockPos pos;
    private final ResourceKey<Level> dim;

    public MiniRequestListMessage(MiniQuarryTile tile) {
        this.pos = tile.getBlockPos();
        this.dim = PacketHandler.getDimension(tile);
    }

    public MiniRequestListMessage(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.dim = ResourceKey.create(Registries.DIMENSION, buf.readResourceLocation());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos).writeResourceLocation(dim.location());
    }

    public static void onReceive(MiniRequestListMessage message, PlayPayloadContext context) {
        context.workHandler().execute(() -> PacketHandler.getWorld(context, message.pos, message.dim)
            .flatMap(l -> l.getBlockEntity(message.pos, Holder.MINI_QUARRY_TYPE))
            .ifPresent(t ->
                context.replyHandler().send(new MiniListSyncMessage(t))));
    }
}
