package com.yogpc.qp.machines.controller;

import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.packet.PacketHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * To Server only.
 */
public final class SetSpawnerEntityMessage implements IMessage {
    private final BlockPos pos;
    private final ResourceKey<Level> dim;
    private final ResourceLocation entity;

    public SetSpawnerEntityMessage(BlockPos pos, ResourceKey<Level> dim, ResourceLocation entity) {
        this.pos = pos;
        this.dim = dim;
        this.entity = entity;
    }

    public SetSpawnerEntityMessage(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.dim = ResourceKey.create(Registries.DIMENSION, buf.readResourceLocation());
        this.entity = buf.readResourceLocation();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos).writeResourceLocation(dim.location());
        buf.writeResourceLocation(entity);
    }

    public static void onReceive(SetSpawnerEntityMessage message, Supplier<NetworkEvent.Context> supplier) {
        supplier.get().enqueueWork(() ->
                PacketHandler.getWorld(supplier.get(), message.pos, message.dim)
                        .ifPresent(level -> BlockController.setSpawnerEntity(level, message.pos, message.entity))
        );
    }
}
