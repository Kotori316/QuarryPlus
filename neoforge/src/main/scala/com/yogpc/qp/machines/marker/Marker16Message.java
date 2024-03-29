package com.yogpc.qp.machines.marker;

import com.yogpc.qp.packet.ClientSyncMessage;
import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.utils.MapMulti;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.util.Objects;

/**
 * To server only.
 */
public final class Marker16Message implements IMessage {

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
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos).writeResourceLocation(dim.location());
        buffer.writeVarInt(amount);
        buffer.writeVarInt(yMax);
        buffer.writeVarInt(yMin);
    }

    public static void onReceive(Marker16Message message, PlayPayloadContext context) {
        var world = PacketHandler.getWorld(context, message.pos, message.dim);
        context.workHandler().execute(() ->
            world.map(w -> w.getBlockEntity(message.pos))
                .flatMap(MapMulti.optCast(Tile16Marker.class))
                .ifPresent(m -> {
                    m.changeSize(message.amount, message.yMax, message.yMin);
                    PacketHandler.sendToClient(new ClientSyncMessage(m), Objects.requireNonNull(m.getLevel()));
                }));
    }

}
