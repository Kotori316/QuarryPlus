package com.yogpc.qp.machines.mini_quarry;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.yogpc.qp.Holder;
import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.packet.PacketHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

/**
 * To both Client and Server.
 */
public final class MiniListSyncMessage implements IMessage {
    private final BlockPos pos;
    private final ResourceKey<Level> dim;
    private final Collection<BlockStatePredicate> allowList;
    private final Collection<BlockStatePredicate> denyList;

    public MiniListSyncMessage(BlockPos pos, ResourceKey<Level> dim, Collection<BlockStatePredicate> denyList, Collection<BlockStatePredicate> allowList) {
        this.pos = pos;
        this.dim = dim;
        this.allowList = allowList;
        this.denyList = denyList;
    }

    public MiniListSyncMessage(MiniQuarryTile tile) {
        this(tile.getBlockPos(), PacketHandler.getDimension(tile), tile.denyList, tile.allowList);
    }

    public MiniListSyncMessage(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.dim = ResourceKey.create(Registries.DIMENSION, buf.readResourceLocation());
        int allowListSize = buf.readInt();
        this.allowList = Stream.generate(buf::readNbt)
            .limit(allowListSize)
            .filter(Objects::nonNull)
            .map(BlockStatePredicate::fromTag)
            .toList();
        int denyListSize = buf.readInt();
        this.denyList = Stream.generate(buf::readNbt)
            .limit(denyListSize)
            .filter(Objects::nonNull)
            .map(BlockStatePredicate::fromTag)
            .toList();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos).writeResourceLocation(dim.location());
        buf.writeInt(allowList.size());
        allowList.stream().map(BlockStatePredicate::toTag).forEach(buf::writeNbt);
        buf.writeInt(denyList.size());
        denyList.stream().map(BlockStatePredicate::toTag).forEach(buf::writeNbt);
    }

    public static void onReceive(MiniListSyncMessage message, Supplier<NetworkEvent.Context> supplier) {
        switch (supplier.get().getDirection().getReceptionSide()) {
            case SERVER -> inServer(message, supplier);
            case CLIENT -> inClient(message, supplier);
        }
    }

    static void inServer(MiniListSyncMessage message, Supplier<NetworkEvent.Context> supplier) {
        // Set lists.
        supplier.get().enqueueWork(() ->
            PacketHandler.getWorld(supplier.get(), message.pos, message.dim)
                .flatMap(w -> w.getBlockEntity(message.pos, Holder.MINI_QUARRY_TYPE))
                .ifPresent(t -> {
                    t.allowList = message.allowList;
                    t.denyList = message.denyList;
                }));
    }

    @OnlyIn(Dist.CLIENT)
    static void inClient(MiniListSyncMessage message, Supplier<NetworkEvent.Context> supplier) {
        // Open GUI of current list.
        supplier.get().enqueueWork(() ->
            PacketHandler.getWorld(supplier.get(), message.pos, message.dim)
                .flatMap(w -> w.getBlockEntity(message.pos, Holder.MINI_QUARRY_TYPE))
                .ifPresent(t ->
                    Minecraft.getInstance().pushGuiLayer(new MiniQuarryListGui(t, message.allowList, message.denyList))));
    }
}
