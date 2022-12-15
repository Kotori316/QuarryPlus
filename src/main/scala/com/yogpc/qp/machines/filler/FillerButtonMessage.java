package com.yogpc.qp.machines.filler;

import java.util.function.Supplier;

import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.packet.PacketHandler;
import com.yogpc.qp.utils.MapMulti;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;

/**
 * To Server only
 */
public final class FillerButtonMessage implements IMessage {
    private final BlockPos pos;
    private final ResourceKey<Level> dim;
    private final FillerEntity.Action action;

    FillerButtonMessage(FillerEntity filler, FillerEntity.Action action) {
        this.pos = filler.getBlockPos();
        this.dim = PacketHandler.getDimension(filler);
        this.action = action;
    }

    public FillerButtonMessage(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.dim = ResourceKey.create(Registries.DIMENSION, buf.readResourceLocation());
        this.action = buf.readEnum(FillerEntity.Action.class);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos).writeResourceLocation(dim.location());
        buf.writeEnum(this.action);
    }

    public static void onReceive(FillerButtonMessage message, Supplier<NetworkEvent.Context> supplier) {
        var world = PacketHandler.getWorld(supplier.get(), message.pos, message.dim);
        supplier.get().enqueueWork(() ->
            world.map(w -> w.getBlockEntity(message.pos))
                .flatMap(MapMulti.optCast(FillerEntity.class))
                .ifPresent(f -> {
                    f.start(message.action);
                    if (f.fillerAction.isFinished()) {
                        // Filler work is not started.
                        PacketHandler.getPlayer(supplier.get()).ifPresent(p ->
                            p.displayClientMessage(Component.literal("Filler work isn't started. You must place a marker near Filler."), false));
                    }
                }));
    }
}
