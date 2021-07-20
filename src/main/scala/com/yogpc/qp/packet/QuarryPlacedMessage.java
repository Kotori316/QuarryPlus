package com.yogpc.qp.packet;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.EnchantmentLevel;
import com.yogpc.qp.machines.quarry.TileQuarry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;

/**
 * To client only.
 */
public class QuarryPlacedMessage implements IMessage<QuarryPlacedMessage> {
    public static final Identifier NAME = new Identifier(QuarryPlus.modID, "quarry_placed_message");
    private BlockPos pos;
    private Identifier dim;
    private List<EnchantmentLevel> levels;
    private NbtCompound otherData;

    public QuarryPlacedMessage() {
    }

    public QuarryPlacedMessage(TileQuarry quarry) {
        this(quarry.getWorld(), quarry.getPos(), quarry.getEnchantments(), quarry.getTileDataForItem());
    }

    public QuarryPlacedMessage(World world, BlockPos pos, List<EnchantmentLevel> levels, NbtCompound otherData) {
        this.levels = levels;
        this.otherData = otherData;
        this.pos = pos;
        this.dim = (world != null ? world.getRegistryKey() : World.OVERWORLD).getValue();
    }

    @Override
    public QuarryPlacedMessage readFromBuffer(PacketByteBuf buffer) {
        pos = buffer.readBlockPos();
        dim = buffer.readIdentifier();
        levels = IntStream.range(0, buffer.readInt())
            .mapToObj(i -> new EnchantmentLevel(buffer.readIdentifier(), buffer.readInt()))
            .toList();
        otherData = buffer.readNbt();
        return this;
    }

    @Override
    public void writeToBuffer(PacketByteBuf buffer) {
        buffer.writeBlockPos(pos).writeIdentifier(dim);
        buffer.writeInt(levels.size());
        for (EnchantmentLevel level : levels) {
            buffer.writeIdentifier(level.enchantmentID()).writeInt(level.level());
        }
        buffer.writeNbt(otherData);
    }

    @Override
    public Identifier getIdentifier() {
        return NAME;
    }

    static final ClientPlayNetworking.PlayChannelHandler HANDLER = (client, handler, buf, responseSender) -> {
        var message = IMessage.decode(QuarryPlacedMessage::new).apply(buf);
        var world = client.world;
        if (world != null && world.getRegistryKey().equals(RegistryKey.of(Registry.WORLD_KEY, message.dim))) {
            if (world.getBlockEntity(message.pos) instanceof TileQuarry quarry) {
                quarry.setEnchantments(message.levels.stream().map(e -> Pair.of(e.enchantment(), e.level())).collect(Collectors.toMap(Pair::getKey, Pair::getValue)));
                quarry.setTileDataFromItem(message.otherData);
            }
        }
    };
}
