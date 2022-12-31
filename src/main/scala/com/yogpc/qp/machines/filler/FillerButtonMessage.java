package com.yogpc.qp.machines.filler;

import java.util.Optional;

import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.packet.IMessage;
import com.yogpc.qp.utils.MapMulti;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/**
 * To Server only
 */
public final class FillerButtonMessage implements IMessage<FillerButtonMessage> {
    public static final ResourceLocation NAME = new ResourceLocation(QuarryPlus.modID, "filler_button_message");
    private final BlockPos pos;
    private final ResourceKey<Level> dim;
    private final FillerEntity.Action action;

    FillerButtonMessage(FillerEntity filler, FillerEntity.Action action) {
        this.pos = filler.getBlockPos();
        this.dim = filler.getLevel() != null ? filler.getLevel().dimension() : Level.OVERWORLD;
        this.action = action;
    }

    public FillerButtonMessage(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.dim = ResourceKey.create(Registries.DIMENSION, buf.readResourceLocation());
        this.action = buf.readEnum(FillerEntity.Action.class);
    }

    @Override
    public void writeToBuffer(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos).writeResourceLocation(dim.location());
        buf.writeEnum(this.action);
    }

    @Override
    public ResourceLocation getIdentifier() {
        return NAME;
    }

    public static final ServerPlayNetworking.PlayChannelHandler handler = (server, player, handler1, buf, responseSender) -> {
        var message = new FillerButtonMessage(buf);
        server.execute(() ->
            Optional.ofNullable(server.getLevel(message.dim))
                .map(w -> w.getBlockEntity(message.pos))
                .flatMap(MapMulti.optCast(FillerEntity.class))
                .ifPresent(f -> {
                    f.start(message.action);
                    if (f.fillerAction.isFinished()) {
                        // Filler work is not started.
                        player.displayClientMessage(Component.literal("Filler work isn't started. You must place a marker near Filler."), false);
                    }
                }));
    };
}
