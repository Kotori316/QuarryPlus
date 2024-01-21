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
public final class FlexMarkerMessage implements IMessage {
    private final BlockPos pos;
    private final ResourceKey<Level> dim;
    private final TileFlexMarker.Movable movable;
    private final int amount;

    public FlexMarkerMessage(Level world, BlockPos pos, TileFlexMarker.Movable movable, int amount) {
        this.pos = pos;
        this.dim = world != null ? world.dimension() : Level.OVERWORLD;
        this.movable = movable;
        this.amount = amount;
    }

    public FlexMarkerMessage(FriendlyByteBuf buffer) {
        pos = buffer.readBlockPos();
        dim = ResourceKey.create(Registries.DIMENSION, buffer.readResourceLocation());
        movable = buffer.readEnum(TileFlexMarker.Movable.class);
        amount = buffer.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos).writeResourceLocation(dim.location());
        buffer.writeEnum(movable);
        buffer.writeVarInt(amount);
    }

    public static void onReceive(FlexMarkerMessage message, PlayPayloadContext context) {
        var world = PacketHandler.getWorld(context, message.pos, message.dim);
        context.workHandler().execute(() ->
            world.map(w -> w.getBlockEntity(message.pos))
                .flatMap(MapMulti.optCast(TileFlexMarker.class))
                .ifPresent(m -> {
                    m.move(message.movable, message.amount);
                    PacketHandler.sendToClient(new ClientSyncMessage(m), Objects.requireNonNull(m.getLevel()));
                }));
    }
}
