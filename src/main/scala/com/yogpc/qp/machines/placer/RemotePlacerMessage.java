package com.yogpc.qp.machines.placer;

import java.util.function.Supplier;

import com.yogpc.qp.Holder;
import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.packet.PacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

/**
 * To Server only.
 */
public record RemotePlacerMessage(BlockPos pos, ResourceKey<Level> dim, BlockPos newTarget) implements IMessage {
    public RemotePlacerMessage(FriendlyByteBuf buf) {
        this(
            buf.readBlockPos(),
            ResourceKey.create(Registry.DIMENSION_REGISTRY, buf.readResourceLocation()),
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

    public static void onReceive(RemotePlacerMessage message, Supplier<NetworkEvent.Context> supplier) {
        var world = PacketHandler.getWorld(supplier.get(), message.pos, message.dim);
        supplier.get().enqueueWork(() ->
            world.flatMap(w -> w.getBlockEntity(message.pos, Holder.REMOTE_PLACER_TYPE))
                .ifPresent(placer -> placer.targetPos = message.newTarget));
    }
}
