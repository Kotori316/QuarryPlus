package com.yogpc.qp.machines.advquarry;

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
public final class AdvQuarryInitialMessage implements IMessage {
    private final BlockPos pos;
    private final ResourceKey<Level> dim;

    private final boolean startImmediately;
    private final boolean placeAreaFrame;
    private final boolean chunkByChunk;

    public AdvQuarryInitialMessage(BlockPos pos, ResourceKey<Level> dim, boolean startImmediately, boolean placeAreaFrame, boolean chunkByChunk) {
        this.pos = pos;
        this.dim = dim;
        this.startImmediately = startImmediately;
        this.placeAreaFrame = placeAreaFrame;
        this.chunkByChunk = chunkByChunk;
    }

    public AdvQuarryInitialMessage(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.dim = ResourceKey.create(Registry.DIMENSION_REGISTRY, buf.readResourceLocation());
        this.startImmediately = buf.readBoolean();
        this.placeAreaFrame = buf.readBoolean();
        this.chunkByChunk = buf.readBoolean();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos).writeResourceLocation(dim.location());
        buf.writeBoolean(startImmediately).writeBoolean(placeAreaFrame).writeBoolean(chunkByChunk);
    }

    public static void onReceive(AdvQuarryInitialMessage message, Supplier<NetworkEvent.Context> supplier) {
        var world = PacketHandler.getWorld(supplier.get(), message.pos, message.dim);
        supplier.get().enqueueWork(() -> world.flatMap(w -> w.getBlockEntity(message.pos, Holder.ADV_QUARRY_TYPE)).ifPresent(t -> {
            t.startImmediately = message.startImmediately;
            t.placeAreaFrame = message.placeAreaFrame;
        }));
    }

    public static class Ask implements IMessage {
        private final BlockPos pos;
        private final ResourceKey<Level> dim;

        public Ask(BlockPos pos, ResourceKey<Level> dim) {
            this.pos = pos;
            this.dim = dim;
        }

        public Ask(FriendlyByteBuf buf) {
            this.pos = buf.readBlockPos();
            this.dim = ResourceKey.create(Registry.DIMENSION_REGISTRY, buf.readResourceLocation());
        }

        @Override
        public void write(FriendlyByteBuf buf) {
            buf.writeBlockPos(pos).writeResourceLocation(dim.location());
        }

        public static void onReceive(Ask message, Supplier<NetworkEvent.Context> supplier) {
            PacketHandler.getWorld(supplier.get(), message.pos, message.dim)
                .flatMap(w -> w.getBlockEntity(message.pos, Holder.ADV_QUARRY_TYPE))
                .ifPresent(t ->
                    PacketHandler.sendToServer(new AdvQuarryInitialMessage(message.pos, message.dim,
                        true, true, false))
                );
        }
    }
}
