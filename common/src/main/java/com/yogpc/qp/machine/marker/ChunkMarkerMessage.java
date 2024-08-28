package com.yogpc.qp.machine.marker;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.packet.OnReceiveWithLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import java.util.Objects;

/**
 * To server
 */
public final class ChunkMarkerMessage implements CustomPacketPayload, OnReceiveWithLevel {
    public static final ResourceLocation NAME = ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, "chunk_marker_message");
    public static final CustomPacketPayload.Type<ChunkMarkerMessage> TYPE = new Type<>(NAME);
    public static final StreamCodec<FriendlyByteBuf, ChunkMarkerMessage> STREAM_CODEC = CustomPacketPayload.codec(
        ChunkMarkerMessage::write, ChunkMarkerMessage::new
    );

    private final BlockPos pos;
    private final ResourceKey<Level> dim;
    private final int size;
    private final int minY;
    private final int maxY;

    public ChunkMarkerMessage(ChunkMarkerEntity marker) {
        pos = marker.getBlockPos();
        dim = Objects.requireNonNull(marker.getLevel()).dimension();
        size = marker.size;
        minY = marker.minY;
        maxY = marker.maxY;
    }

    public ChunkMarkerMessage(FriendlyByteBuf buffer) {
        pos = buffer.readBlockPos();
        dim = ResourceKey.create(Registries.DIMENSION, buffer.readResourceLocation());
        size = buffer.readVarInt();
        minY = buffer.readVarInt();
        maxY = buffer.readVarInt();
    }

    void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos).writeResourceKey(dim);
        buffer.writeVarInt(size);
        buffer.writeVarInt(minY);
        buffer.writeVarInt(maxY);
    }

    @Override
    public void onReceive(Level level) {
        if (!level.dimension().equals(dim)) {
            return;
        }
        if (level.getBlockEntity(pos) instanceof ChunkMarkerEntity marker && marker.enabled) {
            marker.size = size;
            marker.minY = minY;
            marker.maxY = maxY;
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
