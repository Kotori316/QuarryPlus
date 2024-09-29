package com.yogpc.qp.machine.advquarry;

import com.yogpc.qp.PlatformAccess;
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

import java.util.Objects;

/**
 * To client only
 * Also syncs the area because the acknowledgement will replace server setting with client one, it's a problem
 */
public final class AdvQuarryInitialAskMessage implements CustomPacketPayload, OnReceiveWithLevel {
    public static final ResourceLocation NAME = ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, "adv_quarry_initial_ask_message");
    public static final CustomPacketPayload.Type<AdvQuarryInitialAskMessage> TYPE = new Type<>(NAME);
    public static final StreamCodec<FriendlyByteBuf, AdvQuarryInitialAskMessage> STREAM_CODEC = CustomPacketPayload.codec(
        AdvQuarryInitialAskMessage::write, AdvQuarryInitialAskMessage::new
    );
    private final BlockPos pos;
    private final ResourceKey<Level> dim;
    @NotNull
    private final Area area;

    public AdvQuarryInitialAskMessage(BlockPos pos, ResourceKey<Level> dim, @NotNull Area area) {
        this.dim = dim;
        this.pos = pos;
        this.area = area;
    }

    AdvQuarryInitialAskMessage(AdvQuarryEntity entity) {
        this(
            entity.getBlockPos(),
            Objects.requireNonNull(entity.getLevel()).dimension(),
            Objects.requireNonNull(entity.getArea())
        );
    }

    AdvQuarryInitialAskMessage(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.dim = buf.readResourceKey(Registries.DIMENSION);
        this.area = buf.readJsonWithCodec(Area.CODEC.codec());
    }

    void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos).writeResourceKey(this.dim);
        buf.writeJsonWithCodec(Area.CODEC.codec(), this.area);
    }

    @Override
    public void onReceive(Level level, Player player) {
        if (!level.dimension().equals(dim)) {
            return;
        }
        var entity = level.getBlockEntity(pos);
        if (entity instanceof AdvQuarryEntity quarry) {
            quarry.setArea(area);
            if (PlatformAccess.config().noEnergy()) {
                // Not to start immediately to configure area
                quarry.workConfig = WorkConfig.DEFAULT.noAutoStartConfig();
            } else {
                quarry.workConfig = WorkConfig.DEFAULT;
            }
            PlatformAccess.getAccess().packetHandler().sendToServer(new AdvActionSyncMessage(quarry));
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
