package com.yogpc.qp.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * To both client and server.
 */
public final class TileMessage implements IMessage {
    private final CompoundTag tag;
    private final BlockPos pos;
    private final ResourceKey<Level> dim;

    public TileMessage(BlockPos pos, ResourceKey<Level> dim, CompoundTag tag) {
        this.tag = tag;
        this.pos = pos;
        this.dim = dim;
    }

    public TileMessage(BlockEntity entity) {
        this(entity.getBlockPos(), PacketHandler.getDimension(entity), entity.saveWithoutMetadata());
    }

    public TileMessage(FriendlyByteBuf buf) {
        this(buf.readBlockPos(), ResourceKey.create(Registries.DIMENSION, buf.readResourceLocation()), buf.readNbt());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeResourceLocation(this.dim.location());
        buf.writeNbt(this.tag);
    }

    public static void onReceive(TileMessage message, Supplier<NetworkEvent.Context> supplier) {
        var world = PacketHandler.getWorld(supplier.get(), message.pos, message.dim);
        supplier.get().enqueueWork(() -> world.map(l -> l.getBlockEntity(message.pos)).ifPresent(t -> t.load(message.tag)));
    }

}
