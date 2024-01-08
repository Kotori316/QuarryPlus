package com.yogpc.qp.machines.placer;

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
public record RemotePlacerMessage(BlockPos pos, ResourceKey<Level> dim, BlockPos newTarget) implements IMessage {
    public RemotePlacerMessage(FriendlyByteBuf buf) {
        this(
            buf.readBlockPos(),
            ResourceKey.create(Registries.DIMENSION, buf.readResourceLocation()),
            buf.readBlockPos()
        );
    }

    public RemotePlacerMessage(RemotePlacerTile tile, BlockPos newTarget) {
        this(
            tile.getBlockPos(),
            PacketHandler.getDimension(tile),
            newTarget
        );
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos).writeResourceLocation(dim.location());
        buf.writeBlockPos(newTarget);
    }

    public static void onReceive(RemotePlacerMessage message, PlayPayloadContext context) {
        var world = PacketHandler.getWorld(context, message.pos, message.dim);
        context.workHandler().execute(() ->
            world.flatMap(w -> w.getBlockEntity(message.pos, Holder.REMOTE_PLACER_TYPE))
                .ifPresent(placer -> placer.targetPos = message.newTarget));
    }
}
