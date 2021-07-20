package com.yogpc.qp.packet;

import java.util.List;

import javax.annotation.Nonnull;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class PacketHandler {
    public static class Server {
        public static void initServer() {
            var list = List.of(
                new ServerPacketInit(LevelMessage.NAME, LevelMessage.handler)
            );
            list.forEach(i -> ServerPlayNetworking.registerGlobalReceiver(i.name(), i.handler()));
        }

        private record ServerPacketInit(Identifier name, ServerPlayNetworking.PlayChannelHandler handler) {
        }
    }

    @Environment(EnvType.CLIENT)
    public static class Client {
        public static void initClient() {
            var list = List.of(
                new ClientPacketInit(QuarryPlacedMessage.NAME, QuarryPlacedMessage.HANDLER)
            );
            list.forEach(i -> ClientPlayNetworking.registerGlobalReceiver(i.name(), i.handler()));
        }

        private record ClientPacketInit(Identifier name, ClientPlayNetworking.PlayChannelHandler handler) {
        }
    }

    @Environment(EnvType.CLIENT)
    public static void sendToServer(@Nonnull IMessage<?> message) {
        var packet = PacketByteBufs.create();
        message.writeToBuffer(packet);
        ClientPlayNetworking.send(message.getIdentifier(), packet);
    }

    public static void sendToClientPlayer(@Nonnull IMessage<?> message, @Nonnull ServerPlayerEntity player) {
        var packet = PacketByteBufs.create();
        message.writeToBuffer(packet);
        ServerPlayNetworking.send(player, message.getIdentifier(), packet);
    }
}
