package com.yogpc.qp.machines.quarry;

import java.util.Optional;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.packet.IMessage;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/**
 * To server
 */
public final class QuarryConfigSyncMessage implements IMessage<QuarryConfigSyncMessage> {
    public static final ResourceLocation NAME = new ResourceLocation(QuarryPlus.modID, "quarry_config_sync_message");
    private final BlockPos pos;
    private final ResourceKey<Level> dim;
    private final QuarryConfig quarryConfig;

    QuarryConfigSyncMessage(TileQuarry quarry) {
        this(quarry.getBlockPos(), Optional.ofNullable(quarry.getLevel()).map(Level::dimension).orElse(Level.OVERWORLD), quarry.quarryConfig);
    }

    public QuarryConfigSyncMessage(BlockPos pos, ResourceKey<Level> dim, QuarryConfig quarryConfig) {
        this.pos = pos;
        this.dim = dim;
        this.quarryConfig = quarryConfig;
    }

    public QuarryConfigSyncMessage(FriendlyByteBuf buf) {
        this(
            buf.readBlockPos(),
            ResourceKey.create(Registry.DIMENSION_REGISTRY, buf.readResourceLocation()),
            QuarryConfig.fromPacket(buf)
        );
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(this.pos).writeResourceLocation(this.dim.location());
        this.quarryConfig.writePacket(buffer);
    }

    @Override
    public ResourceLocation getIdentifier() {
        return NAME;
    }

    public static final ServerPlayNetworking.PlayChannelHandler handler = (server, player, handler1, buf, responseSender) -> {
        var message = new QuarryConfigSyncMessage(buf);
        server.execute(() ->
            Optional.ofNullable(server.getLevel(message.dim))
                .flatMap(l -> l.getBlockEntity(message.pos, QuarryPlus.ModObjects.QUARRY_TYPE))
                .ifPresent(t -> t.quarryConfig = message.quarryConfig)
        );
    };
}
