package com.yogpc.qp.packet;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.marker.TileFlexMarker;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

/**
 * To server only.
 */
public class FlexMarkerMessage implements IMessage<FlexMarkerMessage> {
    public static final Identifier NAME = new Identifier(QuarryPlus.modID, "flex_message");
    private final BlockPos pos;
    private final RegistryKey<World> dim;
    private final TileFlexMarker.Movable movable;
    private final int amount;

    public FlexMarkerMessage(World world, BlockPos pos, TileFlexMarker.Movable movable, int amount) {
        this.pos = pos;
        this.dim = world != null ? world.getRegistryKey() : World.OVERWORLD;
        this.movable = movable;
        this.amount = amount;
    }

    public FlexMarkerMessage(PacketByteBuf buffer) {
        pos = buffer.readBlockPos();
        dim = RegistryKey.of(Registry.WORLD_KEY, buffer.readIdentifier());
        movable = buffer.readEnumConstant(TileFlexMarker.Movable.class);
        amount = buffer.readVarInt();
    }

    @Override
    public FlexMarkerMessage readFromBuffer(PacketByteBuf buffer) {
        return new FlexMarkerMessage(buffer);
    }

    @Override
    public void writeToBuffer(PacketByteBuf buffer) {
        buffer.writeBlockPos(pos).writeIdentifier(dim.getValue());
        buffer.writeEnumConstant(movable);
        buffer.writeVarInt(amount);
    }

    @Override
    public Identifier getIdentifier() {
        return NAME;
    }

    static final ServerPlayNetworking.PlayChannelHandler handler = (server, player, handler1, buf, responseSender) -> {
        var message = new FlexMarkerMessage(buf);
        server.execute(() -> {
            var world = server.getWorld(message.dim);
            if (world != null) {
                if (world.getBlockEntity(message.pos) instanceof TileFlexMarker flexMarker) {
                    flexMarker.move(message.movable, message.amount);
                    flexMarker.sync();
                }
            }
        });
    };
}
