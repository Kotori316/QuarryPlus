package com.yogpc.qp.packet;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.quarry.TileQuarry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/**
 * To Server only.
 */
public class LevelMessage implements IMessage<LevelMessage> {
    public static final ResourceLocation NAME = new ResourceLocation(QuarryPlus.modID, "y_message");
    private BlockPos pos;
    private ResourceKey<Level> dim;
    private int digMinY;

    public static LevelMessage create(Level world, BlockPos pos, int digMinY) {
        var message = new LevelMessage();
        message.pos = pos;
        message.dim = world != null ? world.dimension() : Level.OVERWORLD;
        message.digMinY = digMinY;
        return message;
    }

    @Override
    public LevelMessage readFromBuffer(FriendlyByteBuf buffer) {
        pos = buffer.readBlockPos();
        dim = ResourceKey.create(Registry.DIMENSION_REGISTRY, buffer.readResourceLocation());
        digMinY = buffer.readInt();
        return this;
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos).writeResourceLocation(dim.location());
        buffer.writeInt(digMinY);
    }

    @Override
    public ResourceLocation getIdentifier() {
        return NAME;
    }

    static final ServerPlayNetworking.PlayChannelHandler handler = (server, player, handler1, buf, responseSender) -> {
        var message = IMessage.decode(LevelMessage::new).apply(buf);
        server.execute(() -> {
            var world = server.getLevel(message.dim);
            if (world != null) {
                if (world.getBlockEntity(message.pos) instanceof TileQuarry quarry) {
                    quarry.digMinY = message.digMinY;
                }
            }
        });
    };
}
