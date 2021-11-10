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
    private final BlockPos pos;
    private final ResourceKey<Level> dim;
    private final int digMinY;

    public LevelMessage(Level world, BlockPos pos, int digMinY) {
        this.pos = pos;
        this.dim = world != null ? world.dimension() : Level.OVERWORLD;
        this.digMinY = digMinY;
    }

    public LevelMessage(FriendlyByteBuf buffer) {
        this.pos = buffer.readBlockPos();
        this.dim = ResourceKey.create(Registry.DIMENSION_REGISTRY, buffer.readResourceLocation());
        this.digMinY = buffer.readInt();
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
        var message = new LevelMessage(buf);
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
