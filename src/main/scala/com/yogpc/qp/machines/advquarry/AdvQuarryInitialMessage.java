package com.yogpc.qp.machines.advquarry;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.packet.PacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * To Server only.
 */
public final class AdvQuarryInitialMessage implements IMessage {
    private final BlockPos pos;
    private final ResourceKey<Level> dim;

    private final WorkConfig workConfig;

    AdvQuarryInitialMessage(BlockPos pos, ResourceKey<Level> dim, WorkConfig workConfig) {
        this.pos = pos;
        this.dim = dim;
        this.workConfig = workConfig;
    }

    public AdvQuarryInitialMessage(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.dim = ResourceKey.create(Registries.DIMENSION, buf.readResourceLocation());
        this.workConfig = new WorkConfig(buf);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos).writeResourceLocation(dim.location());
        workConfig.writePacket(buf);
    }

    public static void onReceive(AdvQuarryInitialMessage message, Supplier<NetworkEvent.Context> supplier) {
        var world = PacketHandler.getWorld(supplier.get(), message.pos, message.dim);
        supplier.get().enqueueWork(() -> world.flatMap(w -> w.getBlockEntity(message.pos, Holder.ADV_QUARRY_TYPE)).ifPresent(t ->
            t.workConfig = message.workConfig
        ));
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
            this.dim = ResourceKey.create(Registries.DIMENSION, buf.readResourceLocation());
        }

        @Override
        public void write(FriendlyByteBuf buf) {
            buf.writeBlockPos(pos).writeResourceLocation(dim.location());
        }

        public static void onReceive(Ask message, Supplier<NetworkEvent.Context> supplier) {
            PacketHandler.getWorld(supplier.get(), message.pos, message.dim)
                .flatMap(w -> w.getBlockEntity(message.pos, Holder.ADV_QUARRY_TYPE))
                .ifPresent(t ->
                    PacketHandler.sendToServer(new AdvQuarryInitialMessage(message.pos, message.dim, getWorkConfig()))
                );
        }

        @NotNull
        private static WorkConfig getWorkConfig() {
            return new WorkConfig(
                QuarryPlus.clientConfig.chunkDestroyerSetting.startImmediately.get(),
                QuarryPlus.clientConfig.chunkDestroyerSetting.placeAreaFrame.get(),
                QuarryPlus.clientConfig.chunkDestroyerSetting.chunkByChunk.get()
            );
        }
    }
}
