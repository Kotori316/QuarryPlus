package com.yogpc.qp.machines.misc;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.packet.IMessage;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
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
        this.dim = ResourceKey.create(Registries.DIMENSION, buffer.readResourceLocation());
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

    public static final ServerPlayNetworking.PlayChannelHandler handler = (server, player, handler1, buf, responseSender) -> {
        var message = new LevelMessage(buf);
        server.execute(() -> {
            var world = server.getLevel(message.dim);
            if (world != null) {
                YAccessor accessor = YAccessor.get(world.getBlockEntity(message.pos));
                if (accessor != null) {
                    accessor.setDigMinY(message.digMinY);
                } else {
                    QuarryPlus.LOGGER.warn("({}) YAccessor for {} is absent. At {} in {}",
                        NAME, world.getBlockEntity(message.pos), message.pos, message.dim);
                }
            }
        });
    };
}
