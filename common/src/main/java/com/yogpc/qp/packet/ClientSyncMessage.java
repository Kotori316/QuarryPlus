package com.yogpc.qp.packet;

import com.yogpc.qp.QuarryPlus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Objects;

/**
 * To client only
 */
public final class ClientSyncMessage implements CustomPacketPayload, OnReceiveWithLevel {
    public static final ResourceLocation NAME = ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, "client_sync_message");
    public static final CustomPacketPayload.Type<ClientSyncMessage> TYPE = new Type<>(NAME);
    public static final StreamCodec<FriendlyByteBuf, ClientSyncMessage> STREAM_CODEC = CustomPacketPayload.codec(
        ClientSyncMessage::write, ClientSyncMessage::new
    );

    private final BlockPos pos;
    private final ResourceKey<Level> dim;
    private final CompoundTag tag;

    public ClientSyncMessage(BlockPos pos, ResourceKey<Level> dim, CompoundTag tag) {
        this.pos = pos;
        this.dim = dim;
        this.tag = tag;
    }

    public <T extends BlockEntity & ClientSync> ClientSyncMessage(T t) {
        this.pos = t.getBlockPos();
        this.dim = Objects.requireNonNull(t.getLevel()).dimension();
        this.tag = t.toClientTag(new CompoundTag(), Objects.requireNonNull(t.getLevel()).registryAccess());
    }

    ClientSyncMessage(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
        this.dim = buffer.readResourceKey(Registries.DIMENSION);
        this.tag = buffer.readNbt();
    }

    void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos).writeResourceKey(dim);
        buffer.writeNbt(tag);
    }

    @Override
    public void onReceive(Level level, Player player) {
        if (!level.dimension().equals(dim)) {
            return;
        }
        var entity = level.getBlockEntity(pos);
        if (entity instanceof ClientSync clientSync) {
            clientSync.fromClientTag(tag, level.registryAccess());
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
