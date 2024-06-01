package com.yogpc.qp.packet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.event.network.CustomPayloadEvent;

import static com.yogpc.qp.utils.MapMulti.optCast;

/**
 * To client only.
 */
public final class ClientSyncMessage implements IMessage {
    private final CompoundTag tag;
    private final BlockPos pos;
    private final ResourceKey<Level> dim;

    private ClientSyncMessage(BlockPos pos, ResourceKey<Level> dim, CompoundTag tag) {
        this.tag = tag;
        this.pos = pos;
        this.dim = dim;
    }

    public <T extends BlockEntity & ClientSync> ClientSyncMessage(T entity) {
        this(entity.getBlockPos(), PacketHandler.getDimension(entity), entity.toClientTag(new CompoundTag()));
    }

    public ClientSyncMessage(FriendlyByteBuf buf) {
        this(buf.readBlockPos(), ResourceKey.create(Registries.DIMENSION, buf.readResourceLocation()), buf.readNbt());
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeResourceLocation(this.dim.location());
        buf.writeNbt(this.tag);
    }

    public static void onReceive(ClientSyncMessage message, CustomPayloadEvent.Context supplier) {
        var world = PacketHandler.getWorld(supplier, message.pos, message.dim);
        if (supplier.get().getDirection().getReceptionSide() != LogicalSide.CLIENT) {
            throw new IllegalStateException("Message was sent to unexpected side.");
        }
        supplier.enqueueWork(() ->
            world.map(l -> l.getBlockEntity(message.pos))
                .flatMap(optCast(ClientSync.class))
                .ifPresent(t -> t.fromClientTag(message.tag)));
    }
}
