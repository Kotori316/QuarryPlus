package com.yogpc.qp.packet;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.marker.Tile16Marker;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public class Marker16Message implements IMessage<Marker16Message> {
    public static final Identifier NAME = new Identifier(QuarryPlus.modID, "marker16_message");

    private final BlockPos pos;
    private final Identifier dim;
    private final int amount;
    private final int yMax;
    private final int yMin;

    public Marker16Message(World world, BlockPos pos, int amount, int yMax, int yMin) {
        this.pos = pos;
        this.dim = (world != null ? world.getRegistryKey() : World.OVERWORLD).getValue();
        this.amount = amount;
        this.yMax = yMax;
        this.yMin = yMin;
    }

    public Marker16Message(PacketByteBuf buffer) {
        pos = buffer.readBlockPos();
        dim = buffer.readIdentifier();
        amount = buffer.readVarInt();
        yMax = buffer.readVarInt();
        yMin = buffer.readVarInt();
    }

    @Override
    public Marker16Message readFromBuffer(PacketByteBuf buffer) {
        return new Marker16Message(buffer);
    }

    @Override
    public void writeToBuffer(PacketByteBuf buffer) {
        buffer.writeBlockPos(pos).writeIdentifier(dim);
        buffer.writeVarInt(amount);
        buffer.writeVarInt(yMax);
        buffer.writeVarInt(yMin);
    }

    @Override
    public Identifier getIdentifier() {
        return NAME;
    }

    static final ServerPlayNetworking.PlayChannelHandler handler = (server, player, handler1, buf, responseSender) -> {
        var message = new Marker16Message(buf);
        server.execute(() -> {
            var world = server.getWorld(RegistryKey.of(Registry.WORLD_KEY, message.dim));
            if (world != null) {
                if (world.getBlockEntity(message.pos) instanceof Tile16Marker marker) {
                    marker.changeSize(message.amount, message.yMax, message.yMin);
                    marker.sync();
                }
            }
        });
    };
}
