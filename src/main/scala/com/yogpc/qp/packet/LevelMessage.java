package com.yogpc.qp.packet;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.quarry.TileQuarry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

/**
 * To Server only.
 */
public class LevelMessage implements IMessage<LevelMessage> {
    public static final Identifier NAME = new Identifier(QuarryPlus.modID, "y_message");
    private BlockPos pos;
    private RegistryKey<World> dim;
    private int digMinY;

    public static LevelMessage create(World world, BlockPos pos, int digMinY) {
        var message = new LevelMessage();
        message.pos = pos;
        message.dim = world != null ? world.getRegistryKey() : World.OVERWORLD;
        message.digMinY = digMinY;
        return message;
    }

    @Override
    public LevelMessage readFromBuffer(PacketByteBuf buffer) {
        pos = buffer.readBlockPos();
        dim = RegistryKey.of(Registry.WORLD_KEY, buffer.readIdentifier());
        digMinY = buffer.readInt();
        return this;
    }

    @Override
    public void writeToBuffer(PacketByteBuf buffer) {
        buffer.writeBlockPos(pos).writeIdentifier(dim.getValue());
        buffer.writeInt(digMinY);
    }

    @Override
    public Identifier getIdentifier() {
        return NAME;
    }

    static final ServerPlayNetworking.PlayChannelHandler handler = (server, player, handler1, buf, responseSender) -> {
        var message = IMessage.decode(LevelMessage::new).apply(buf);
        server.execute(() -> {
            var world = server.getWorld(message.dim);
            if (world != null) {
                if (world.getBlockEntity(message.pos) instanceof TileQuarry quarry) {
                    quarry.digMinY = message.digMinY;
                }
            }
        });
    };
}
