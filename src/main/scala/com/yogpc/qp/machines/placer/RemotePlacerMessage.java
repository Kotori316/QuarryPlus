package com.yogpc.qp.machines.placer;

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
 * To Server only.
 */
public record RemotePlacerMessage(BlockPos pos, ResourceKey<Level> dim,
                                  BlockPos newTarget) implements IMessage<RemotePlacerMessage> {
    public static final ResourceLocation NAME = new ResourceLocation(QuarryPlus.modID, "remote_placer_message");

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
            tile.getLevel() != null ? tile.getLevel().dimension() : Level.OVERWORLD,
            newTarget
        );
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos).writeResourceLocation(dim.location());
        buf.writeBlockPos(newTarget);
    }

    @Override
    public ResourceLocation getIdentifier() {
        return NAME;
    }

    public static final ServerPlayNetworking.PlayChannelHandler handler = (server, player, handler1, buf, responseSender) -> {
        var message = new RemotePlacerMessage(buf);
        server.execute(() -> {
            var world = server.getLevel(message.dim);
            if (world != null) {
                world.getBlockEntity(message.pos, QuarryPlus.ModObjects.REMOTE_PLACER_TYPE)
                    .ifPresent(placer -> placer.targetPos = message.newTarget);
            }
        });
    };
}
