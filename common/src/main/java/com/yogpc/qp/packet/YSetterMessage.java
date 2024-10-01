package com.yogpc.qp.packet;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machine.misc.YAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
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
 * To server only
 */
public final class YSetterMessage implements CustomPacketPayload, OnReceiveWithLevel {
    public static final ResourceLocation NAME = ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, "y_set_message");
    public static final CustomPacketPayload.Type<YSetterMessage> TYPE = new Type<>(NAME);
    public static final StreamCodec<FriendlyByteBuf, YSetterMessage> STREAM_CODEC = CustomPacketPayload.codec(
        YSetterMessage::write, YSetterMessage::new
    );

    private final BlockPos pos;
    private final ResourceKey<Level> dim;
    private final int digMinY;

    public YSetterMessage(BlockPos pos, ResourceKey<Level> dim, int digMinY) {
        this.pos = pos;
        this.dim = dim;
        this.digMinY = digMinY;
    }

    public YSetterMessage(BlockEntity t, int digMinY) {
        this(
            t.getBlockPos(),
            Objects.requireNonNull(t.getLevel()).dimension(),
            digMinY
        );
    }

    YSetterMessage(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
        this.dim = buffer.readResourceKey(Registries.DIMENSION);
        this.digMinY = buffer.readVarInt();
    }

    void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeResourceKey(dim);
        buffer.writeVarInt(digMinY);
    }

    @Override
    public void onReceive(Level level, Player player) {
        if (!level.dimension().equals(dim)) {
            return;
        }
        var entity = level.getBlockEntity(pos);
        var accessor = YAccessor.get(entity);
        if (accessor != null) {
            accessor.digMinY().setMinY(digMinY);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
