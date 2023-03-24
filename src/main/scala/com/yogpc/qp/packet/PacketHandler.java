package com.yogpc.qp.packet;

import java.util.List;

import com.yogpc.qp.machines.advquarry.AdvActionMessage;
import com.yogpc.qp.machines.filler.FillerButtonMessage;
import com.yogpc.qp.machines.misc.LevelMessage;
import com.yogpc.qp.machines.placer.RemotePlacerMessage;
import com.yogpc.qp.machines.quarry.QuarryConfigSyncMessage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class PacketHandler {
    public static class Server {
        public static void initServer() {
            var list = List.of(
                new ServerPacketInit(QuarryConfigSyncMessage.NAME, QuarryConfigSyncMessage.handler),
                new ServerPacketInit(LevelMessage.NAME, LevelMessage.handler),
                new ServerPacketInit(AdvActionMessage.NAME, AdvActionMessage.handler),
                new ServerPacketInit(FlexMarkerMessage.NAME, FlexMarkerMessage.handler),
                new ServerPacketInit(Marker16Message.NAME, Marker16Message.handler),
                new ServerPacketInit(FillerButtonMessage.NAME, FillerButtonMessage.handler),
                new ServerPacketInit(RemotePlacerMessage.NAME, RemotePlacerMessage.handler)
            );
            list.forEach(i -> ServerPlayNetworking.registerGlobalReceiver(i.name(), i.handler()));
        }

        private record ServerPacketInit(ResourceLocation name, ServerPlayNetworking.PlayChannelHandler handler) {
        }
    }

    @Environment(EnvType.CLIENT)
    public static class Client {
        public static void initClient() {
            var list = List.of(
                new ClientPacketInit(QuarryPlacedMessage.NAME, QuarryPlacedMessage.HandlerHolder.HANDLER),
                new ClientPacketInit(ClientSyncMessage.NAME, ClientSyncMessage.HandlerHolder.HANDLER)
            );
            list.forEach(i -> ClientPlayNetworking.registerGlobalReceiver(i.name(), i.handler()));
        }

        private record ClientPacketInit(ResourceLocation name, ClientPlayNetworking.PlayChannelHandler handler) {
        }
    }

    @Environment(EnvType.CLIENT)
    public static void sendToServer(@NotNull IMessage<?> message) {
        var packet = PacketByteBufs.create();
        message.writeToBuffer(packet);
        ClientPlayNetworking.send(message.getIdentifier(), packet);
    }

    public static void sendToClientPlayer(@NotNull IMessage<?> message, @NotNull ServerPlayer player) {
        var packet = PacketByteBufs.create();
        message.writeToBuffer(packet);
        ServerPlayNetworking.send(player, message.getIdentifier(), packet);
    }

    public static void sendToClientWorld(@NotNull IMessage<?> message, Level level) {
        var packet = PacketByteBufs.create();
        message.writeToBuffer(packet);
        for (ServerPlayer player : PlayerLookup.world((ServerLevel) level)) {
            ServerPlayNetworking.send(player, message.getIdentifier(), packet);
        }
    }
}
