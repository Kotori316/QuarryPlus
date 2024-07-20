package com.yogpc.qp.fabric.packet;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.packet.ClientSyncMessage;
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
        public static void initServer() {
            // Use ServerPlayNetworking.registerGlobalReceiver
        }
    }

    @Environment(EnvType.CLIENT)
    public static class Client {
        public static void initClient() {
            PayloadTypeRegistry.playS2C().register(ClientSyncMessage.TYPE, ClientSyncMessage.STREAM_CODEC);

            ClientPlayNetworking.registerGlobalReceiver(ClientSyncMessage.TYPE, Client::clientSyncOnReceive);
        }

        private static void clientSyncOnReceive(ClientSyncMessage message, ClientPlayNetworking.Context context) {
            var level = context.client().level;
            context.client().execute(() -> message.onReceive(level));
        }
    }

    public void sendToClientWorld(@NotNull CustomPacketPayload message, @NotNull Level level) {
        for (ServerPlayer player : PlayerLookup.world((ServerLevel) level)) {
            ServerPlayNetworking.send(player, message);
        }
    }
}
