package com.yogpc.qp.machine.advquarry;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.Area;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * To server only
 */
public final class AdvActionSyncMessage implements CustomPacketPayload, OnReceiveWithLevel {
    public static final ResourceLocation NAME = ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, "adv_action_sync_message");
    public static final CustomPacketPayload.Type<AdvActionSyncMessage> TYPE = new Type<>(NAME);
    public static final StreamCodec<FriendlyByteBuf, AdvActionSyncMessage> STREAM_CODEC = CustomPacketPayload.codec(
        AdvActionSyncMessage::write, AdvActionSyncMessage::new
    );
    private final BlockPos pos;
    private final ResourceKey<Level> dim;
    @Nullable
    private final Area area;
    @NotNull
    private final WorkConfig workConfig;
    private final boolean syncArea;

    AdvActionSyncMessage(BlockPos pos, ResourceKey<Level> dim, @Nullable Area area, @NotNull WorkConfig workConfig, boolean syncArea) {
        this.pos = pos;
        this.dim = dim;
        this.area = area;
        this.workConfig = workConfig;
        this.syncArea = syncArea;
    }

    AdvActionSyncMessage(AdvQuarryEntity entity, boolean syncArea) {
        this(
            entity.getBlockPos(),
            Objects.requireNonNull(entity.getLevel()).dimension(),
            entity.getArea(),
            entity.workConfig,
            syncArea
        );
    }

    AdvActionSyncMessage(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
        this.dim = buffer.readResourceKey(Registries.DIMENSION);
        boolean shouldAreaLoad = buffer.readBoolean();
        if (shouldAreaLoad) {
            this.area = buffer.readJsonWithCodec(Area.CODEC.codec());
        } else {
            this.area = null;
        }
        this.workConfig = buffer.readJsonWithCodec(WorkConfig.CODEC.codec());
        this.syncArea = buffer.readBoolean();
    }

    void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos).writeResourceKey(dim);
        if (area == null) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);
            buffer.writeJsonWithCodec(Area.CODEC.codec(), area);
        }
        buffer.writeJsonWithCodec(WorkConfig.CODEC.codec(), workConfig);
        buffer.writeBoolean(syncArea);
    }

    @Override
    public void onReceive(Level level, Player player) {
        if (!level.dimension().equals(dim)) {
            return;
        }
        var entity = level.getBlockEntity(pos);
        if (entity instanceof AdvQuarryEntity quarry && quarry.enabled) {
            if (syncArea) {
                quarry.setArea(area);
            }
            quarry.workConfig = workConfig;
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AdvActionSyncMessage that = (AdvActionSyncMessage) o;
        return syncArea == that.syncArea && Objects.equals(pos, that.pos) && Objects.equals(dim, that.dim) && Objects.equals(area, that.area) && Objects.equals(workConfig, that.workConfig);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos, dim, area, workConfig, syncArea);
    }
}
