package com.yogpc.qp.machine.advquarry;

import com.yogpc.qp.PlatformAccess;
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

public final class AdvActionActionMessage implements CustomPacketPayload, OnReceiveWithLevel {
    public static final ResourceLocation NAME = ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, "adv_action_action_message");
    public static final CustomPacketPayload.Type<AdvActionActionMessage> TYPE = new Type<>(NAME);
    public static final StreamCodec<FriendlyByteBuf, AdvActionActionMessage> STREAM_CODEC = CustomPacketPayload.codec(
        AdvActionActionMessage::write, AdvActionActionMessage::new
    );
    private final BlockPos pos;
    private final ResourceKey<Level> dim;
    private final Action action;

    AdvActionActionMessage(BlockPos pos, ResourceKey<Level> dim, Action action) {
        this.action = action;
        this.pos = pos;
        this.dim = dim;
    }

    AdvActionActionMessage(AdvQuarryEntity entity, Action action) {
        this(
            entity.getBlockPos(),
            Objects.requireNonNull(entity.getLevel()).dimension(),
            action
        );
    }

    AdvActionActionMessage(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
        this.dim = buffer.readResourceKey(Registries.DIMENSION);
        this.action = buffer.readEnum(Action.class);
    }

    void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos).writeResourceKey(dim);
        buffer.writeEnum(action);
    }

    @Override
    public void onReceive(Level level, Player player) {
        if (!level.dimension().equals(dim)) {
            return;
        }
        var entity = level.getBlockEntity(pos);
        if (entity instanceof AdvQuarryEntity quarry && quarry.enabled) {
            switch (action) {
                case MODULE_INV -> {
                    if (!PlatformAccess.getAccess().platformName().equalsIgnoreCase("fabric")) {

                    }
                }
                case QUICK_START -> {
                    if (quarry.currentState == AdvQuarryState.WAITING) {
                        quarry.workConfig = quarry.workConfig.startSoonConfig();
                        quarry.startQuarryWork();
                    }
                }
            }
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    enum Action {
        QUICK_START, MODULE_INV
    }
}
