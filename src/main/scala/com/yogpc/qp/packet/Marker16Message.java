package com.yogpc.qp.packet;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.marker.Tile16Marker;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/**
 * To server only.
 */
public class Marker16Message implements IMessage<Marker16Message> {
    public static final ResourceLocation NAME = new ResourceLocation(QuarryPlus.modID, "marker16_message");

    private final BlockPos pos;
    private final ResourceKey<Level> dim;
    private final int amount;
    private final int yMax;
    private final int yMin;

    public Marker16Message(Level world, BlockPos pos, int amount, int yMax, int yMin) {
        this.pos = pos;
        this.dim = world != null ? world.dimension() : Level.OVERWORLD;
        this.amount = amount;
        this.yMax = yMax;
        this.yMin = yMin;
    }

    public Marker16Message(FriendlyByteBuf buffer) {
        pos = buffer.readBlockPos();
        dim = ResourceKey.create(Registries.DIMENSION, buffer.readResourceLocation());
        amount = buffer.readVarInt();
        yMax = buffer.readVarInt();
        yMin = buffer.readVarInt();
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos).writeResourceLocation(dim.location());
        buffer.writeVarInt(amount);
        buffer.writeVarInt(yMax);
        buffer.writeVarInt(yMin);
    }

    @Override
    public ResourceLocation getIdentifier() {
        return NAME;
    }

    static final ServerPlayNetworking.PlayChannelHandler handler = (server, player, handler1, buf, responseSender) -> {
        var message = new Marker16Message(buf);
        server.execute(() -> {
            var world = server.getLevel(message.dim);
            if (world != null) {
                if (world.getBlockEntity(message.pos) instanceof Tile16Marker marker) {
                    marker.changeSize(message.amount, message.yMax, message.yMin);
                    marker.sync();
                }
            }
        });
    };
}
