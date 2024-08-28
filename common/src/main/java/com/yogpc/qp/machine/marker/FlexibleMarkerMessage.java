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

public final class FlexibleMarkerMessage implements CustomPacketPayload, OnReceiveWithLevel {
    public static final ResourceLocation NAME = ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, "flexible_marker_message");
    public static final CustomPacketPayload.Type<FlexibleMarkerMessage> TYPE = new Type<>(NAME);
    public static final StreamCodec<FriendlyByteBuf, FlexibleMarkerMessage> STREAM_CODEC = CustomPacketPayload.codec(
        FlexibleMarkerMessage::write, FlexibleMarkerMessage::new
    );
    private final BlockPos pos;
    private final ResourceKey<Level> dim;
    private final FlexibleMarkerEntity.Movable movable;
    private final int amount;

    public FlexibleMarkerMessage(BlockPos pos, ResourceKey<Level> dim, FlexibleMarkerEntity.Movable movable, int amount) {
        this.pos = pos;
        this.dim = dim;
        this.movable = movable;
        this.amount = amount;
    }

    public FlexibleMarkerMessage(FlexibleMarkerEntity marker, FlexibleMarkerEntity.Movable movable, int amount) {
        this(
            marker.getBlockPos(),
            Objects.requireNonNull(marker.getLevel()).dimension(),
            movable,
            amount
        );
    }

    FlexibleMarkerMessage(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
        this.dim = ResourceKey.create(Registries.DIMENSION, buffer.readResourceLocation());
        this.movable = buffer.readEnum(FlexibleMarkerEntity.Movable.class);
        this.amount = buffer.readInt();
    }

    void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos).writeResourceKey(dim);
        buffer.writeEnum(movable);
        buffer.writeInt(amount);
    }

    @Override
    public void onReceive(Level level) {
        if (!level.dimension().equals(dim)) {
            return;
        }
        if (level.getBlockEntity(pos) instanceof FlexibleMarkerEntity marker && marker.enabled) {
            marker.move(movable, amount);
            marker.syncToClient();
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
