package com.yogpc.qp.packet;

import com.yogpc.qp.QuarryPlus;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/**
 * To Client Only.
 */
public final class ClientSyncMessage implements IMessage<ClientSyncMessage> {
    public static final ResourceLocation NAME = new ResourceLocation(QuarryPlus.modID, "client_sync_message");
    private final CompoundTag tag;
    private final BlockPos pos;
    private final ResourceKey<Level> dim;

    public ClientSyncMessage(BlockPos pos, ResourceKey<Level> dim, CompoundTag tag) {
        this.tag = tag;
        this.pos = pos;
        this.dim = dim;
    }

    public ClientSyncMessage(FriendlyByteBuf buf) {
        this(buf.readBlockPos(), ResourceKey.create(Registry.DIMENSION_REGISTRY, buf.readResourceLocation()), buf.readNbt());
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeResourceLocation(this.dim.location());
        buf.writeNbt(this.tag);
    }

    @Override
    public ResourceLocation getIdentifier() {
        return NAME;
    }

    @Environment(EnvType.CLIENT)
    static class HandlerHolder {
        static final ClientPlayNetworking.PlayChannelHandler HANDLER = (client, handler, buf, responseSender) -> {
            var message = new ClientSyncMessage(buf);
            var world = client.level;
            if (world != null && world.dimension().equals(message.dim)) {
                client.execute(() -> {
                    if (world.getBlockEntity(message.pos) instanceof ClientSync tile) {
                        tile.fromClientTag(message.tag);
                    }
                });
            }
        };
    }
}
