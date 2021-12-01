package com.yogpc.qp.machines.marker;

import java.util.Objects;
import java.util.function.Supplier;

import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.packet.TileMessage;
import com.yogpc.qp.utils.MapMulti;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

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
        dim = ResourceKey.create(Registry.DIMENSION_REGISTRY, buffer.readResourceLocation());
        movable = buffer.readEnum(TileFlexMarker.Movable.class);
        amount = buffer.readVarInt();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos).writeResourceLocation(dim.location());
        buffer.writeEnum(movable);
        buffer.writeVarInt(amount);
    }

    public static void onReceive(FlexMarkerMessage message, Supplier<NetworkEvent.Context> supplier) {
        var world = PacketHandler.getWorld(supplier.get(), message.pos, message.dim);
        supplier.get().enqueueWork(() ->
            world.map(w -> w.getBlockEntity(message.pos))
                .flatMap(MapMulti.optCast(TileFlexMarker.class))
                .ifPresent(m -> {
                    m.move(message.movable, message.amount);
                    PacketHandler.sendToClient(new TileMessage(m), Objects.requireNonNull(m.getLevel()));
                }));
    }
}
