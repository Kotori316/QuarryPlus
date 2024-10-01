package com.yogpc.qp.fabric.machine.quarry;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.packet.OnReceiveWithLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Objects;

/**
 * To Server
 */
public final class QuarryConfigSyncMessage implements CustomPacketPayload, OnReceiveWithLevel {
    public static final ResourceLocation NAME = ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, "quarry_message");
    public static final CustomPacketPayload.Type<QuarryConfigSyncMessage> TYPE = new Type<>(NAME);
    public static final StreamCodec<FriendlyByteBuf, QuarryConfigSyncMessage> STREAM_CODEC = CustomPacketPayload.codec(
        QuarryConfigSyncMessage::write, QuarryConfigSyncMessage::new
    );

    private final BlockPos pos;
    private final ResourceKey<Level> dim;
    private final boolean shouldRemoveFluid;

    public QuarryConfigSyncMessage(BlockPos pos, ResourceKey<Level> dim, boolean shouldRemoveFluid) {
        this.pos = pos;
        this.dim = dim;
        this.shouldRemoveFluid = shouldRemoveFluid;
    }

    public QuarryConfigSyncMessage(QuarryEntityFabric t, boolean shouldRemoveFluid) {
        this(
            t.getBlockPos(),
            Objects.requireNonNull(t.getLevel()).dimension(),
            shouldRemoveFluid
        );
    }

    QuarryConfigSyncMessage(FriendlyByteBuf byteBuf) {
        this.pos = byteBuf.readBlockPos();
        this.dim = byteBuf.readResourceKey(Registries.DIMENSION);
        this.shouldRemoveFluid = byteBuf.readBoolean();
    }

    void write(FriendlyByteBuf byteBuf) {
        byteBuf.writeBlockPos(pos);
        byteBuf.writeResourceKey(dim);
        byteBuf.writeBoolean(shouldRemoveFluid);
    }

    @Override
    public void onReceive(Level level, Player player) {
        if (!level.dimension().equals(dim)) {
            return;
        }
        var entity = level.getBlockEntity(pos);
        if (entity instanceof QuarryEntityFabric quarry) {
            quarry.shouldRemoveFluid = shouldRemoveFluid;
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
