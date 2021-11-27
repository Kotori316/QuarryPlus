package com.yogpc.qp.packet;

import java.util.function.Supplier;

import com.yogpc.qp.machines.misc.YAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

/**
 * To Server only.
 */
public final class LevelMessage implements IMessage {
    private final BlockPos pos;
    private final ResourceKey<Level> dim;
    private final int digMinY;

    public LevelMessage(Level world, BlockPos pos, int digMinY) {
        this.pos = pos;
        this.dim = world != null ? world.dimension() : Level.OVERWORLD;
        this.digMinY = digMinY;
    }

    public LevelMessage(FriendlyByteBuf buffer) {
        pos = buffer.readBlockPos();
        dim = ResourceKey.create(Registry.DIMENSION_REGISTRY, buffer.readResourceLocation());
        digMinY = buffer.readInt();
    }

    @Override
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBlockPos(pos).writeResourceLocation(dim.location());
        buffer.writeInt(digMinY);

    }

    public static void onReceive(LevelMessage message, Supplier<NetworkEvent.Context> supplier) {
        var world = PacketHandler.getWorld(supplier.get(), message.pos, message.dim);
        supplier.get().enqueueWork(() ->
            world
                .map(l -> l.getBlockEntity(message.pos))
                .map(YAccessor::get)
                .ifPresent(yAccessor -> yAccessor.setDigMinY(message.digMinY))
        );
    }

}