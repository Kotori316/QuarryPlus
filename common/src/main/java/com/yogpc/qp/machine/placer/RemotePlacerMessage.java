package com.yogpc.qp.machine.placer;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.packet.OnReceiveWithLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Objects;

/**
 * To Server only.
 */
public record RemotePlacerMessage(BlockPos pos, ResourceKey<Level> dim,
                                  BlockPos newTarget) implements CustomPacketPayload, OnReceiveWithLevel {
    public static final Identifier NAME = Identifier.fromNamespaceAndPath(QuarryPlus.modID, "remote_placer_message");
    public static final CustomPacketPayload.Type<RemotePlacerMessage> TYPE = new Type<>(NAME);
    public static final StreamCodec<RegistryFriendlyByteBuf, RemotePlacerMessage> STREAM_CODEC = CustomPacketPayload.codec(
        RemotePlacerMessage::write, RemotePlacerMessage::new
    );

    public RemotePlacerMessage(FriendlyByteBuf buf) {
        this(
            buf.readBlockPos(),
            ResourceKey.create(Registries.DIMENSION, buf.readIdentifier()),
            buf.readBlockPos()
        );
    }

    public RemotePlacerMessage(RemotePlacerEntity tile, BlockPos newTarget) {
        this(
            tile.getBlockPos(),
            Objects.requireNonNull(tile.getLevel()).dimension(),
            newTarget
        );
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos).writeIdentifier(dim.identifier());
        buf.writeBlockPos(newTarget);
    }

    @Override
    public void onReceive(Level level, Player player) {
        if (!level.dimension().equals(dim)) {
            return;
        }
        if (level.getBlockEntity(pos) instanceof RemotePlacerEntity placer) {
            placer.targetPos = newTarget;
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
