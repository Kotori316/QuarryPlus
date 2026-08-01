package com.yogpc.qp.machine.advpump;

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
import org.jetbrains.annotations.VisibleForTesting;

import java.util.Objects;

/**
 * Client to server only. Carries the Frame/Delete toggle state from {@link AdvPumpScreen}'s buttons.
 */
public final class AdvPumpSettingsMessage implements CustomPacketPayload, OnReceiveWithLevel {
    public static final ResourceLocation NAME = ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, "adv_pump_settings_message");
    public static final CustomPacketPayload.Type<AdvPumpSettingsMessage> TYPE = new Type<>(NAME);
    public static final StreamCodec<FriendlyByteBuf, AdvPumpSettingsMessage> STREAM_CODEC = CustomPacketPayload.codec(
        AdvPumpSettingsMessage::write, AdvPumpSettingsMessage::new
    );
    private final BlockPos pos;
    private final ResourceKey<Level> dim;
    private final boolean placeFrame;
    private final boolean deleteFluid;

    AdvPumpSettingsMessage(BlockPos pos, ResourceKey<Level> dim, boolean placeFrame, boolean deleteFluid) {
        this.pos = pos;
        this.dim = dim;
        this.placeFrame = placeFrame;
        this.deleteFluid = deleteFluid;
    }

    AdvPumpSettingsMessage(AdvPumpEntity entity) {
        this(
            entity.getBlockPos(),
            Objects.requireNonNull(entity.getLevel()).dimension(),
            entity.placeFrame,
            entity.deleteFluid
        );
    }

    AdvPumpSettingsMessage(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
        this.dim = buffer.readResourceKey(Registries.DIMENSION);
        this.placeFrame = buffer.readBoolean();
        this.deleteFluid = buffer.readBoolean();
    }

    void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos).writeResourceKey(dim);
        buffer.writeBoolean(placeFrame);
        buffer.writeBoolean(deleteFluid);
    }

    @Override
    public void onReceive(Level level, Player player) {
        if (!level.dimension().equals(dim)) {
            return;
        }
        if (level.getBlockEntity(pos) instanceof AdvPumpEntity pump && pump.enabled) {
            pump.placeFrame = this.placeFrame;
            pump.deleteFluid = this.deleteFluid;
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @VisibleForTesting
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AdvPumpSettingsMessage that)) return false;
        return placeFrame == that.placeFrame && deleteFluid == that.deleteFluid
            && Objects.equals(pos, that.pos) && Objects.equals(dim, that.dim);
    }

    @VisibleForTesting
    @Override
    public int hashCode() {
        return Objects.hash(pos, dim, placeFrame, deleteFluid);
    }
}
