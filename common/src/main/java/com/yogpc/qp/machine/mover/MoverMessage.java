package com.yogpc.qp.machine.mover;

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
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Objects;

/**
 * To server only
 */
public final class MoverMessage implements CustomPacketPayload, OnReceiveWithLevel {
    public static final ResourceLocation NAME = ResourceLocation.fromNamespaceAndPath(QuarryPlus.modID, "mover_message");
    public static final CustomPacketPayload.Type<MoverMessage> TYPE = new Type<>(NAME);
    public static final StreamCodec<FriendlyByteBuf, MoverMessage> STREAM_CODEC = CustomPacketPayload.codec(
        MoverMessage::write, MoverMessage::new
    );

    private final BlockPos pos;
    private final ResourceKey<Level> dim;
    private final ResourceKey<Enchantment> key;

    MoverMessage(BlockPos pos, ResourceKey<Level> dim, ResourceKey<Enchantment> key) {
        this.pos = pos;
        this.dim = dim;
        this.key = key;
    }

    MoverMessage(BlockEntity entity, ResourceKey<Enchantment> key) {
        this(
            entity.getBlockPos(),
            Objects.requireNonNull(entity.getLevel()).dimension(),
            key
        );
    }

    MoverMessage(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
        this.dim = buffer.readResourceKey(Registries.DIMENSION);
        this.key = buffer.readResourceKey(Registries.ENCHANTMENT);
    }

    void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos);
        buffer.writeResourceKey(dim);
        buffer.writeResourceKey(key);
    }

    @Override
    public void onReceive(Level level, Player player) {
        if (!level.dimension().equals(dim)) {
            return;
        }
        var entity = level.getBlockEntity(pos);
        if (entity instanceof MoverEntity mover && mover.enabled) {
            var enchantment = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(key);
            mover.moveEnchant(enchantment);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
