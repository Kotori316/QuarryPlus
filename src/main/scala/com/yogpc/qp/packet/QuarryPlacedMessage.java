package com.yogpc.qp.packet;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.EnchantmentLevel;
import com.yogpc.qp.machines.quarry.TileQuarry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.tuple.Pair;

/**
 * To client only.
 */
public class QuarryPlacedMessage implements IMessage<QuarryPlacedMessage> {
    public static final ResourceLocation NAME = new ResourceLocation(QuarryPlus.modID, "quarry_placed_message");
    private final BlockPos pos;
    private final ResourceKey<Level> dim;
    private final List<EnchantmentLevel> levels;
    private final CompoundTag otherData;

    public QuarryPlacedMessage(TileQuarry quarry) {
        this(quarry.getLevel(), quarry.getBlockPos(), quarry.getEnchantments(), quarry.getTileDataForItem());
    }

    public QuarryPlacedMessage(Level world, BlockPos pos, List<EnchantmentLevel> levels, CompoundTag otherData) {
        this.levels = levels;
        this.otherData = otherData;
        this.pos = pos;
        this.dim = world != null ? world.dimension() : Level.OVERWORLD;
    }

    public QuarryPlacedMessage(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
        this.dim = ResourceKey.create(Registry.DIMENSION_REGISTRY, buffer.readResourceLocation());
        this.levels = IntStream.range(0, buffer.readInt())
            .mapToObj(i -> new EnchantmentLevel(buffer.readResourceLocation(), buffer.readInt()))
            .toList();
        this.otherData = buffer.readNbt();
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos).writeResourceLocation(dim.location());
        buffer.writeInt(levels.size());
        for (EnchantmentLevel level : levels) {
            buffer.writeResourceLocation(Objects.requireNonNull(level.enchantmentID())).writeInt(level.level());
        }
        buffer.writeNbt(otherData);
    }

    @Override
    public ResourceLocation getIdentifier() {
        return NAME;
    }

    @Environment(EnvType.CLIENT)
    static class HandlerHolder {
        static final ClientPlayNetworking.PlayChannelHandler HANDLER = (client, handler, buf, responseSender) -> {
            var message = new QuarryPlacedMessage(buf);
            var world = client.level;
            if (world != null && world.dimension().equals(message.dim)) {
                client.execute(() -> {
                    if (world.getBlockEntity(message.pos) instanceof TileQuarry quarry) {
                        quarry.setEnchantments(message.levels.stream().map(e -> Pair.of(e.enchantment(), e.level())).collect(Collectors.toMap(Pair::getKey, Pair::getValue)));
                        quarry.setTileDataFromItem(message.otherData);
                    }
                });
            }
        };
    }
}
