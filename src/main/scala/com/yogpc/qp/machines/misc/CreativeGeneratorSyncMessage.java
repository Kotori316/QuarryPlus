package com.yogpc.qp.machines.misc;

import com.yogpc.qp.Holder;
import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.packet.PacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * To server only
 */
public final class CreativeGeneratorSyncMessage implements IMessage {

    private final BlockPos pos;
    private final ResourceKey<Level> dim;
    private final long sendEnergy;

    CreativeGeneratorSyncMessage(BlockPos pos, ResourceKey<Level> dim, long sendEnergy) {
        this.pos = pos;
        this.dim = dim;
        this.sendEnergy = sendEnergy;
    }

    CreativeGeneratorSyncMessage(CreativeGeneratorTile tile) {
        this(tile.getBlockPos(), PacketHandler.getDimension(tile), tile.sendEnergy);
    }

    public CreativeGeneratorSyncMessage(FriendlyByteBuf buf) {
        this(
            buf.readBlockPos(),
            ResourceKey.create(Registry.DIMENSION_REGISTRY, buf.readResourceLocation()),
            buf.readLong()
        );
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos).writeResourceLocation(dim.location());
        buf.writeLong(sendEnergy);
    }

    public static void onReceive(CreativeGeneratorSyncMessage message, Supplier<NetworkEvent.Context> supplier) {
        var world = PacketHandler.getWorld(supplier.get(), message.pos, message.dim);
        supplier.get().enqueueWork(() ->
            world.flatMap(l -> l.getBlockEntity(message.pos, Holder.CREATIVE_GENERATOR_TYPE))
                .ifPresent(t -> t.sendEnergy = message.sendEnergy)
        );
    }
}
