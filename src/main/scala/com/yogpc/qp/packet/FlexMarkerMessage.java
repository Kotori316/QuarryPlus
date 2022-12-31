package com.yogpc.qp.packet;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.marker.TileFlexMarker;
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
public class FlexMarkerMessage implements IMessage<FlexMarkerMessage> {
    public static final ResourceLocation NAME = new ResourceLocation(QuarryPlus.modID, "flex_message");
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
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos).writeResourceLocation(dim.location());
        buffer.writeEnum(movable);
        buffer.writeVarInt(amount);
    }

    @Override
    public ResourceLocation getIdentifier() {
        return NAME;
    }

    static final ServerPlayNetworking.PlayChannelHandler handler = (server, player, handler1, buf, responseSender) -> {
        var message = new FlexMarkerMessage(buf);
        server.execute(() -> {
            var world = server.getLevel(message.dim);
            if (world != null) {
                if (world.getBlockEntity(message.pos) instanceof TileFlexMarker flexMarker) {
                    flexMarker.move(message.movable, message.amount);
                    flexMarker.sync();
                }
            }
        });
    };
}
