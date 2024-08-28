package com.yogpc.qp.fabric.packet;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.fabric.machine.quarry.QuarryConfigSyncMessage;
import com.yogpc.qp.machine.marker.ChunkMarkerMessage;
import com.yogpc.qp.machine.marker.FlexibleMarkerMessage;
import com.yogpc.qp.machine.mover.MoverMessage;
import com.yogpc.qp.packet.ClientSyncMessage;
import com.yogpc.qp.packet.OnReceiveWithLevel;
import com.yogpc.qp.packet.YSetterMessage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public final class PacketHandler implements PlatformAccess.Packet {
    public static class Server {
        public static void registerMessage() {
            PayloadTypeRegistry.playS2C().register(ClientSyncMessage.TYPE, ClientSyncMessage.STREAM_CODEC);
            PayloadTypeRegistry.playC2S().register(YSetterMessage.TYPE, YSetterMessage.STREAM_CODEC);
            PayloadTypeRegistry.playC2S().register(MoverMessage.TYPE, MoverMessage.STREAM_CODEC);
            PayloadTypeRegistry.playC2S().register(QuarryConfigSyncMessage.TYPE, QuarryConfigSyncMessage.STREAM_CODEC);
            PayloadTypeRegistry.playC2S().register(FlexibleMarkerMessage.TYPE, FlexibleMarkerMessage.STREAM_CODEC);
            PayloadTypeRegistry.playC2S().register(ChunkMarkerMessage.TYPE, ChunkMarkerMessage.STREAM_CODEC);
        }

        public static void initServer() {
            ServerPlayNetworking.registerGlobalReceiver(YSetterMessage.TYPE, Server::onReceive);
            ServerPlayNetworking.registerGlobalReceiver(MoverMessage.TYPE, Server::onReceive);
            ServerPlayNetworking.registerGlobalReceiver(QuarryConfigSyncMessage.TYPE, Server::onReceive);
            ServerPlayNetworking.registerGlobalReceiver(FlexibleMarkerMessage.TYPE, Server::onReceive);
            ServerPlayNetworking.registerGlobalReceiver(ChunkMarkerMessage.TYPE, Server::onReceive);
        }

        private static void onReceive(OnReceiveWithLevel message, ServerPlayNetworking.Context context) {
            var level = context.player().level();
            context.server().execute(() -> message.onReceive(level));
        }
    }

    @Environment(EnvType.CLIENT)
    public static class Client {
        public static void initClient() {
            ClientPlayNetworking.registerGlobalReceiver(ClientSyncMessage.TYPE, Client::onReceive);
        }

        private static void onReceive(OnReceiveWithLevel message, ClientPlayNetworking.Context context) {
            var level = context.client().level;
            context.client().execute(() -> message.onReceive(level));
        }
    }

    public void sendToClientWorld(@NotNull CustomPacketPayload message, @NotNull Level level) {
        for (ServerPlayer player : PlayerLookup.world((ServerLevel) level)) {
            ServerPlayNetworking.send(player, message);
        }
    }

    @Override
    public void sendToServer(@NotNull CustomPacketPayload message) {
        ClientPlayNetworking.send(message);
    }
}
